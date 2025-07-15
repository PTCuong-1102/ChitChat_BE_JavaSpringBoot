package com.chitchat.backend.controller;

import com.chitchat.backend.dto.UserResponse;
import com.chitchat.backend.security.UserPrincipal;
import com.chitchat.backend.service.FriendsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        List<UserResponse> friends = friendsService.getFriends(userPrincipal.getId());
        return ResponseEntity.ok(friends);
    }

    /**
     * Send friend request by email
     */
    @PostMapping("/requests")
    public ResponseEntity<Void> sendFriendRequest(@Valid @RequestBody Map<String, String> request,
                                                  Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String email = request.get("email");
        friendsService.sendFriendRequest(userPrincipal.getId(), email);
        return ResponseEntity.ok().build();
    }

    /**
     * Get friend requests for current user
     */
    @GetMapping("/requests")
    public ResponseEntity<List<Map<String, Object>>> getFriendRequests(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        List<Map<String, Object>> requests = friendsService.getFriendRequests(userPrincipal.getId());
        return ResponseEntity.ok(requests);
    }

    /**
     * Accept friend request
     */
    @PutMapping("/requests/{requestId}/accept")
    public ResponseEntity<Void> acceptFriendRequest(@PathVariable UUID requestId,
                                                    Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        friendsService.acceptFriendRequest(userPrincipal.getId(), requestId);
        return ResponseEntity.ok().build();
    }

    /**
     * Reject friend request
     */
    @PutMapping("/requests/{requestId}/reject")
    public ResponseEntity<Void> rejectFriendRequest(@PathVariable UUID requestId,
                                                    Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        friendsService.rejectFriendRequest(userPrincipal.getId(), requestId);
        return ResponseEntity.ok().build();
    }

    /**
     * Remove friend
     */
    @DeleteMapping("/{friendId}")
    public ResponseEntity<Void> removeFriend(@PathVariable UUID friendId,
                                             Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        friendsService.removeFriend(userPrincipal.getId(), friendId);
        return ResponseEntity.ok().build();
    }
}
