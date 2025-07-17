package com.chitchat.backend.util;

import com.chitchat.backend.exception.UnauthorizedException;
import com.chitchat.backend.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public class SecurityUtils {

    /**
     * Safely extract UserPrincipal from Authentication
     * @param authentication The authentication object
     * @return UserPrincipal if valid, throws UnauthorizedException if null
     */
    public static UserPrincipal getUserPrincipal(Authentication authentication) {
        if (authentication == null) {
            throw new UnauthorizedException("Authentication is required");
        }
        
        Object principal = authentication.getPrincipal();
        if (principal == null) {
            throw new UnauthorizedException("User principal is null");
        }
        
        if (!(principal instanceof UserPrincipal)) {
            throw new UnauthorizedException("Invalid user principal type");
        }
        
        return (UserPrincipal) principal;
    }
    
    /**
     * Safely extract User ID from Authentication
     * @param authentication The authentication object
     * @return User ID if valid, throws UnauthorizedException if null
     */
    public static UUID getUserId(Authentication authentication) {
        UserPrincipal userPrincipal = getUserPrincipal(authentication);
        UUID userId = userPrincipal.getId();
        
        if (userId == null) {
            throw new UnauthorizedException("User ID is null");
        }
        
        return userId;
    }
    
    /**
     * Get current user principal from security context
     * @return UserPrincipal if authenticated, throws UnauthorizedException if not
     */
    public static UserPrincipal getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return getUserPrincipal(authentication);
    }
    
    /**
     * Get current user ID from security context
     * @return User ID if authenticated, throws UnauthorizedException if not
     */
    public static UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return getUserId(authentication);
    }
    
    /**
     * Check if user is authenticated
     * @return true if authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return authentication != null && 
                   authentication.isAuthenticated() && 
                   authentication.getPrincipal() instanceof UserPrincipal;
        } catch (Exception e) {
            return false;
        }
    }
} 