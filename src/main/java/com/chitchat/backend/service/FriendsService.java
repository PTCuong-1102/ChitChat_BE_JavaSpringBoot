package com.chitchat.backend.service;

import com.chitchat.backend.dto.UserResponse;
import com.chitchat.backend.entity.User;
import com.chitchat.backend.entity.UserContact;
import com.chitchat.backend.entity.ContactStatus;
import com.chitchat.backend.exception.ResourceNotFoundException;
import com.chitchat.backend.repository.UserContactRepository;
import com.chitchat.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class FriendsService {

    private static final Logger logger = LoggerFactory.getLogger(FriendsService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserContactRepository userContactRepository;

    /**
     * Get all friends for a user
     */
    public List<UserResponse> getFriends(UUID userId) {
        logger.debug("Getting friends for user: {}", userId);
        
        List<UserContact> friendContacts = userContactRepository.findFriendsByUserId(userId);
        
        List<UserResponse> friends = new ArrayList<>();
        for (UserContact contact : friendContacts) {
            User friend = userRepository.findById(contact.getFriendId()).orElse(null);
            if (friend != null) {
                friends.add(UserResponse.fromUser(friend));
            }
        }
        
        logger.debug("Found {} friends for user: {}", friends.size(), userId);
        return friends;
    }

    /**
     * Send friend request by email
     */
    public void sendFriendRequest(UUID senderId, String email) {
        logger.info("Sending friend request from user: {} to email: {}", senderId, email);
        
        User recipient = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        
        // Check if already friends or request exists
        Optional<UserContact> existingContact = userContactRepository.findByUserIdAndFriendId(senderId, recipient.getId());
        if (existingContact.isPresent()) {
            throw new IllegalStateException("Friend request already exists or users are already friends");
        }
        
        // Create friend request
        UserContact friendRequest = new UserContact();
        friendRequest.setUserId(senderId);
        friendRequest.setFriendId(recipient.getId());
        friendRequest.setStatus(ContactStatus.PENDING);
        
        userContactRepository.save(friendRequest);
        logger.info("Friend request sent successfully");
    }

    /**
     * Get friend requests for a user
     */
    public List<Map<String, Object>> getFriendRequests(UUID userId) {
        logger.debug("Getting friend requests for user: {}", userId);
        
        List<UserContact> pendingRequests = userContactRepository.findPendingRequestsByUserId(userId);
        
        List<Map<String, Object>> requests = new ArrayList<>();
        for (UserContact request : pendingRequests) {
            User sender = userRepository.findById(request.getUserId()).orElse(null);
            if (sender != null) {
                Map<String, Object> requestMap = new HashMap<>();
                requestMap.put("id", request.getId());
                requestMap.put("senderId", request.getUserId());
                requestMap.put("receiverId", request.getFriendId());
                requestMap.put("status", request.getStatus().toString());
                requestMap.put("createdAt", request.getCreatedAt());
                requestMap.put("sender", UserResponse.fromUser(sender));
                requests.add(requestMap);
            }
        }
        
        logger.debug("Found {} friend requests for user: {}", requests.size(), userId);
        return requests;
    }

    /**
     * Accept friend request
     */
    public void acceptFriendRequest(UUID userId, UUID requestId) {
        logger.info("Accepting friend request: {} by user: {}", requestId, userId);
        
        UserContact request = userContactRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend request not found"));
        
        if (!request.getFriendId().equals(userId)) {
            throw new IllegalStateException("User can only accept requests sent to them");
        }
        
        if (request.getStatus() != ContactStatus.PENDING) {
            throw new IllegalStateException("Request is not pending");
        }
        
        // Update request status
        request.setStatus(ContactStatus.ACCEPTED);
        userContactRepository.save(request);
        
        // Create reciprocal friendship
        UserContact reciprocal = new UserContact();
        reciprocal.setUserId(userId);
        reciprocal.setFriendId(request.getUserId());
        reciprocal.setStatus(ContactStatus.ACCEPTED);
        userContactRepository.save(reciprocal);
        
        logger.info("Friend request accepted successfully");
    }

    /**
     * Reject friend request
     */
    public void rejectFriendRequest(UUID userId, UUID requestId) {
        logger.info("Rejecting friend request: {} by user: {}", requestId, userId);
        
        UserContact request = userContactRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend request not found"));
        
        if (!request.getFriendId().equals(userId)) {
            throw new IllegalStateException("User can only reject requests sent to them");
        }
        
        if (request.getStatus() != ContactStatus.PENDING) {
            throw new IllegalStateException("Request is not pending");
        }
        
        // Update request status
        request.setStatus(ContactStatus.REJECTED);
        userContactRepository.delete(request);
        
        logger.info("Friend request rejected successfully");
    }

    /**
     * Remove friend
     */
    public void removeFriend(UUID userId, UUID friendId) {
        logger.info("Removing friend: {} by user: {}", friendId, userId);
        
        // Find and remove both directions of friendship
        Optional<UserContact> contact1 = userContactRepository.findByUserIdAndFriendId(userId, friendId);
        Optional<UserContact> contact2 = userContactRepository.findByUserIdAndFriendId(friendId, userId);
        
        contact1.ifPresent(userContactRepository::delete);
        contact2.ifPresent(userContactRepository::delete);
        
        logger.info("Friend removed successfully");
    }
}
