package com.chitchat.backend.controller;

import com.chitchat.backend.dto.UserResponse;
import com.chitchat.backend.security.UserPrincipal;
import com.chitchat.backend.service.FriendsService;
import com.chitchat.backend.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

@RestController
@RequestMapping("/api/friends")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FriendsController {

    @Autowired
    private FriendsService friendsService;

    /**
     * Get all friends for current user
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getFriends(Authentication authentication) {
        UUID userId = SecurityUtils.getUserId(authentication);
        List<UserResponse> friends = friendsService.getFriends(userId);
        return ResponseEntity.ok(friends);
    }

    /**
     * Send friend request by email
     */
    @PostMapping("/requests")
    public ResponseEntity<Void> sendFriendRequest(@Valid @RequestBody Map<String, String> request,
                                                  Authentication authentication) {
        UUID userId = SecurityUtils.getUserId(authentication);
        String email = request.get("email");
        friendsService.sendFriendRequest(userId, email);
        return ResponseEntity.ok().build();
    }

    /**
     * Get friend requests for current user
     */
    @GetMapping("/requests")
    public ResponseEntity<List<Map<String, Object>>> getFriendRequests(Authentication authentication) {
        UUID userId = SecurityUtils.getUserId(authentication);
        List<Map<String, Object>> requests = friendsService.getFriendRequests(userId);
        return ResponseEntity.ok(requests);
    }

    /**
     * Accept friend request
     */
    @PutMapping("/requests/{requestId}/accept")
    public ResponseEntity<Void> acceptFriendRequest(@PathVariable UUID requestId,
                                                    Authentication authentication) {
        UUID userId = SecurityUtils.getUserId(authentication);
        friendsService.acceptFriendRequest(userId, requestId);
        return ResponseEntity.ok().build();
    }

    /**
     * Reject friend request
     */
    @PutMapping("/requests/{requestId}/reject")
    public ResponseEntity<Void> rejectFriendRequest(@PathVariable UUID requestId,
                                                    Authentication authentication) {
        UUID userId = SecurityUtils.getUserId(authentication);
        friendsService.rejectFriendRequest(userId, requestId);
        return ResponseEntity.ok().build();
    }

    /**
     * Remove friend
     */
    @DeleteMapping("/{friendId}")
    public ResponseEntity<Void> removeFriend(@PathVariable UUID friendId,
                                             Authentication authentication) {
        UUID userId = SecurityUtils.getUserId(authentication);
        friendsService.removeFriend(userId, friendId);
        return ResponseEntity.ok().build();
    }

    /**
     * Debug endpoint to check friendship data
     */
    @GetMapping("/debug")
    public ResponseEntity<Map<String, Object>> debugFriendships(Authentication authentication) {
        UUID userId = SecurityUtils.getUserId(authentication);
        Map<String, Object> debug = friendsService.debugFriendships(userId);
        return ResponseEntity.ok(debug);
    }

    /**
     * Create test friendships (DEBUG ONLY)
     */
    @PostMapping("/debug/create-test-friendships")
    public ResponseEntity<Map<String, Object>> createTestFriendships(Authentication authentication) {
        UUID userId = SecurityUtils.getUserId(authentication);
        Map<String, Object> result = friendsService.createTestFriendships(userId);
        return ResponseEntity.ok(result);
    }

    /**
     * Manual friendship creation for testing (DEBUG ONLY)
     */
    @PostMapping("/debug/create-friendship")
    public ResponseEntity<Map<String, Object>> createFriendship(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        UUID userId = SecurityUtils.getUserId(authentication);
        String friendEmail = request.get("friendEmail");
        
        Map<String, Object> result = friendsService.createDirectFriendship(userId, friendEmail);
        return ResponseEntity.ok(result);
    }
}
