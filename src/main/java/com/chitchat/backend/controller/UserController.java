package com.chitchat.backend.controller;

import com.chitchat.backend.dto.UserResponse;
import com.chitchat.backend.security.UserPrincipal;
import com.chitchat.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Search users by query (username, email, or full name)
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam("q") String query,
                                                         Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        List<UserResponse> users = userService.searchUsers(query, userPrincipal.getId());
        return ResponseEntity.ok(users);
    }

    /**
     * Find user by query and return with friendship status
     */
    @GetMapping("/find")
    public ResponseEntity<Map<String, Object>> findUser(@RequestParam("q") String query,
                                                       Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Map<String, Object> result = userService.findUser(query, userPrincipal.getId());
        
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(result);
    }
}
