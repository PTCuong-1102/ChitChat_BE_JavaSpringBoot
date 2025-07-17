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
        Set<UUID> friendIds = new HashSet<>();
        
        for (UserContact contact : friendContacts) {
            UUID friendId = contact.getUserId().equals(userId) ? contact.getFriendId() : contact.getUserId();
            if (!friendIds.contains(friendId)) {
                User friend = userRepository.findById(friendId).orElse(null);
                if (friend != null && friend.getStatus() == true) {
                    friends.add(UserResponse.fromUser(friend));
                    friendIds.add(friendId);
                }
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
        
        // Update original request status to ACCEPTED
        request.setStatus(ContactStatus.ACCEPTED);
        request.setIsActive(true);
        userContactRepository.save(request);
        
        // Create reciprocal friendship (from accepter to sender)
        // The original request is from request.getUserId() to request.getFriendId()
        // We need to create the reverse: from request.getFriendId() to request.getUserId()
        Optional<UserContact> existingReciprocal = userContactRepository.findByUserIdAndFriendId(userId, request.getUserId());
        
        if (existingReciprocal.isPresent()) {
            // Update existing reciprocal relationship
            UserContact reciprocal = existingReciprocal.get();
            reciprocal.setStatus(ContactStatus.ACCEPTED);
            reciprocal.setIsActive(true);
            userContactRepository.save(reciprocal);
            logger.info("Updated existing reciprocal friendship");
        } else {
            // Create new reciprocal friendship
            UserContact reciprocal = new UserContact();
            reciprocal.setUserId(userId);                // The person who accepted
            reciprocal.setFriendId(request.getUserId()); // The person who sent the request
            reciprocal.setStatus(ContactStatus.ACCEPTED);
            reciprocal.setIsActive(true);
            userContactRepository.save(reciprocal);
            logger.info("Created new reciprocal friendship");
        }
        
        logger.info("Friend request accepted successfully - bidirectional friendship created");
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

    /**
     * Debug method to check friendship data
     */
    public Map<String, Object> debugFriendships(UUID userId) {
        logger.debug("Debugging friendships for user: {}", userId);
        
        Map<String, Object> debugInfo = new HashMap<>();
        
        // Get current user info
        User currentUser = userRepository.findById(userId).orElse(null);
        debugInfo.put("currentUser", currentUser != null ? Map.of(
            "id", currentUser.getId(),
            "email", currentUser.getEmail(),
            "status", currentUser.getStatus()
        ) : null);
        
        // Get all contacts for this user (regardless of filters)
        List<UserContact> allContacts = userContactRepository.findContactsByUserId(userId);
        debugInfo.put("allContactsCount", allContacts.size());
        debugInfo.put("allContacts", allContacts.stream().map(contact -> Map.of(
            "id", contact.getId(),
            "userId", contact.getUserId(),
            "friendId", contact.getFriendId(),
            "status", contact.getStatus().toString(),
            "isActive", contact.getIsActive()
        )).collect(Collectors.toList()));
        
        // Get friend contacts using original query
        List<UserContact> friendContacts = userContactRepository.findFriendsByUserId(userId);
        debugInfo.put("friendContactsCount", friendContacts.size());
        debugInfo.put("friendContacts", friendContacts.stream().map(contact -> Map.of(
            "id", contact.getId(),
            "userId", contact.getUserId(),
            "friendId", contact.getFriendId(),
            "status", contact.getStatus().toString(),
            "isActive", contact.getIsActive()
        )).collect(Collectors.toList()));
        
        // Get all users in database with their status
        List<User> allUsers = userRepository.findAll();
        debugInfo.put("totalUsersCount", allUsers.size());
        debugInfo.put("activeUsersCount", allUsers.stream().mapToInt(u -> u.getStatus() ? 1 : 0).sum());
        
        return debugInfo;
    }

    /**
     * Create test friendships for debugging
     */
    public Map<String, Object> createTestFriendships(UUID userId) {
        logger.info("Creating test friendships for user: {}", userId);
        
        Map<String, Object> result = new HashMap<>();
        
        // Get all active users except current user
        List<User> allUsers = userRepository.findAll();
        List<User> otherActiveUsers = allUsers.stream()
            .filter(user -> user.getStatus() && !user.getId().equals(userId))
            .collect(Collectors.toList());
        
        if (otherActiveUsers.isEmpty()) {
            result.put("error", "No other active users found to create friendships with");
            return result;
        }
        
        // Create friendship with first available user
        User testFriend = otherActiveUsers.get(0);
        
        // Create bidirectional friendship
        UserContact friendship1 = new UserContact();
        friendship1.setUserId(userId);
        friendship1.setFriendId(testFriend.getId());
        friendship1.setStatus(ContactStatus.ACCEPTED);
        friendship1.setIsActive(true);
        
        UserContact friendship2 = new UserContact();
        friendship2.setUserId(testFriend.getId());
        friendship2.setFriendId(userId);
        friendship2.setStatus(ContactStatus.ACCEPTED);
        friendship2.setIsActive(true);
        
        userContactRepository.save(friendship1);
        userContactRepository.save(friendship2);
        
        result.put("message", "Test friendship created successfully");
        result.put("friendEmail", testFriend.getEmail());
        result.put("friendId", testFriend.getId());
        result.put("totalOtherActiveUsers", otherActiveUsers.size());
        
        return result;
    }

    /**
     * Create direct friendship for debugging purposes
     */
    public Map<String, Object> createDirectFriendship(UUID userId, String friendEmail) {
        logger.info("Creating direct friendship for user: {} with friend: {}", userId, friendEmail);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Find the friend user
            User friend = userRepository.findByEmail(friendEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + friendEmail));
            
            // Check if friendship already exists
            Optional<UserContact> existingFriendship = userContactRepository.findByUserIdAndFriendId(userId, friend.getId());
            if (existingFriendship.isPresent()) {
                result.put("error", "Friendship already exists");
                return result;
            }
            
            // Create bidirectional friendship directly
            UserContact friendship1 = new UserContact();
            friendship1.setUserId(userId);
            friendship1.setFriendId(friend.getId());
            friendship1.setStatus(ContactStatus.ACCEPTED);
            friendship1.setIsActive(true);
            
            UserContact friendship2 = new UserContact();
            friendship2.setUserId(friend.getId());
            friendship2.setFriendId(userId);
            friendship2.setStatus(ContactStatus.ACCEPTED);
            friendship2.setIsActive(true);
            
            userContactRepository.save(friendship1);
            userContactRepository.save(friendship2);
            
            result.put("message", "Direct friendship created successfully");
            result.put("friendEmail", friendEmail);
            result.put("friendId", friend.getId());
            result.put("userId", userId);
            
            logger.info("Direct friendship created successfully between {} and {}", userId, friend.getId());
            
        } catch (Exception e) {
            logger.error("Failed to create direct friendship: {}", e.getMessage());
            result.put("error", "Failed to create friendship: " + e.getMessage());
        }
        
        return result;
    }
}
