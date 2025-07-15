package com.chitchat.backend.service;

import com.chitchat.backend.dto.UserResponse;
import com.chitchat.backend.entity.User;
import com.chitchat.backend.entity.UserContact;
import com.chitchat.backend.entity.ContactStatus;
import com.chitchat.backend.repository.UserRepository;
import com.chitchat.backend.repository.UserContactRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserContactRepository userContactRepository;

    /**
     * Search users by query (username, email, or full name)
     */
    public List<UserResponse> searchUsers(String query, UUID currentUserId) {
        logger.debug("Searching users with query: {} by user: {}", query, currentUserId);
        
        List<User> users = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(
                query, query, query);
        
        // Filter out current user
        users = users.stream()
                .filter(user -> !user.getId().equals(currentUserId))
                .collect(Collectors.toList());
        
        List<UserResponse> userResponses = users.stream()
                .map(UserResponse::fromUser)
                .collect(Collectors.toList());
        
        logger.debug("Found {} users for query: {}", userResponses.size(), query);
        return userResponses;
    }

    /**
     * Find user by query and return with friendship status
     */
    public Map<String, Object> findUser(String query, UUID currentUserId) {
        logger.debug("Finding user with query: {} by user: {}", query, currentUserId);
        
        Optional<User> userOpt = userRepository.findByUsernameOrEmail(query, query);
        if (userOpt.isEmpty()) {
            logger.debug("User not found with query: {}", query);
            return null;
        }
        
        User user = userOpt.get();
        if (user.getId().equals(currentUserId)) {
            logger.debug("User found but is current user, returning null");
            return null;
        }
        
        // Check friendship status
        String status = "none";
        Optional<UserContact> contact = userContactRepository.findByUserIdAndFriendId(currentUserId, user.getId());
        if (contact.isPresent()) {
            ContactStatus contactStatus = contact.get().getStatus();
            if (contactStatus == ContactStatus.ACCEPTED) {
                status = "friends";
            } else if (contactStatus == ContactStatus.PENDING) {
                status = "pending";
            }
        } else {
            // Check if there's a pending request from the other user
            Optional<UserContact> reverseContact = userContactRepository.findByUserIdAndFriendId(user.getId(), currentUserId);
            if (reverseContact.isPresent() && reverseContact.get().getStatus() == ContactStatus.PENDING) {
                status = "received";
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("user", UserResponse.fromUser(user));
        result.put("status", status);
        
        logger.debug("Found user: {} with status: {}", user.getUsername(), status);
        return result;
    }
}
