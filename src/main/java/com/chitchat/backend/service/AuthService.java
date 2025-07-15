package com.chitchat.backend.service;

import com.chitchat.backend.dto.*;
import com.chitchat.backend.entity.AuthUser;
import com.chitchat.backend.entity.User;
import com.chitchat.backend.exception.ChitChatException;
import com.chitchat.backend.repository.AuthUserRepository;
import com.chitchat.backend.repository.UserRepository;
import com.chitchat.backend.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AuthUserRepository authUserRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    /**
     * Register a new user
     */
    public AuthResponse registerUser(RegisterRequest registerRequest) {
        logger.info("Registering new user: {}", registerRequest.getUsername());
        
        // Check if username already exists
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new ChitChatException("Username is already taken", "USERNAME_ALREADY_EXISTS");
        }
        
        // Check if email already exists in both User and AuthUser tables
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new ChitChatException("Email is already in use", "EMAIL_ALREADY_EXISTS");
        }
        
        if (authUserRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new ChitChatException("Email is already in use", "EMAIL_ALREADY_EXISTS");
        }
        
        // Create new user profile
        User user = new User();
        user.setFullName(registerRequest.getFullName());
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        // Don't set password on User entity (it's transient)
        
        User savedUser = userRepository.save(user);
        
        // Create new auth user with credentials
        AuthUser authUser = new AuthUser();
        authUser.setEmail(registerRequest.getEmail());
        authUser.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        
        authUserRepository.save(authUser);
        
        // Generate JWT token
        String jwt = tokenProvider.generateTokenFromUsername(savedUser.getUsername());
        
        logger.info("User registered successfully: {}", savedUser.getUsername());
        
        return new AuthResponse(jwt, UserResponse.fromUser(savedUser));
    }
    
    /**
     * Authenticate user and generate JWT token
     */
    public AuthResponse loginUser(LoginRequest loginRequest) {
        logger.info("Attempting to authenticate user: {}", loginRequest.getUsernameOrEmail());
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        String jwt = tokenProvider.generateToken(authentication);
        
        // Get user details
        User user = userRepository.findByEmailOrUsername(
                loginRequest.getUsernameOrEmail(),
                loginRequest.getUsernameOrEmail()
        ).orElseThrow(() -> new ChitChatException("User not found", "USER_NOT_FOUND"));
        
        logger.info("User authenticated successfully: {}", user.getUsername());
        
        return new AuthResponse(jwt, UserResponse.fromUser(user));
    }
    
    /**
     * Get current user profile
     */
    public UserResponse getCurrentUser(String username) {
        logger.debug("Getting current user profile: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ChitChatException("User not found", "USER_NOT_FOUND"));
        
        return UserResponse.fromUser(user);
    }
    
    /**
     * Update user profile
     */
    public UserResponse updateUserProfile(String username, UpdateProfileRequest updateRequest) {
        logger.info("Updating user profile: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ChitChatException("User not found", "USER_NOT_FOUND"));
        
        // Update user fields
        if (updateRequest.getFullName() != null) {
            user.setFullName(updateRequest.getFullName());
        }
        
        if (updateRequest.getAvatarUrl() != null) {
            user.setAvatarUrl(updateRequest.getAvatarUrl());
        }
        
        if (updateRequest.getStatus() != null) {
            user.setStatus(updateRequest.getStatus());
        }
        
        User updatedUser = userRepository.save(user);
        
        logger.info("User profile updated successfully: {}", updatedUser.getUsername());
        
        return UserResponse.fromUser(updatedUser);
    }
    
    /**
     * Change user password
     */
    public void changePassword(String username, ChangePasswordRequest changePasswordRequest) {
        logger.info("Changing password for user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ChitChatException("User not found", "USER_NOT_FOUND"));
        
        // Get the AuthUser record for password verification
        AuthUser authUser = authUserRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new ChitChatException("Auth user not found", "AUTH_USER_NOT_FOUND"));
        
        // Verify current password
        if (!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), authUser.getPasswordHash())) {
            throw new ChitChatException("Current password is incorrect", "INVALID_CURRENT_PASSWORD");
        }
        
        // Update password in AuthUser table
        authUser.setPasswordHash(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        authUserRepository.save(authUser);
        
        logger.info("Password changed successfully for user: {}", username);
    }
}
