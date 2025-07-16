package com.chitchat.backend.security;

import com.chitchat.backend.entity.AuthUser;
import com.chitchat.backend.entity.User;
import com.chitchat.backend.repository.AuthUserRepository;
import com.chitchat.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AuthUserRepository authUserRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByEmailOrUsername(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail));
        
        // Load the password from AuthUser table
        AuthUser authUser = authUserRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Auth user not found for email: " + user.getEmail()));
        
        // Set the password in the user object (transient field)
        user.setPassword(authUser.getPasswordHash());

        return UserPrincipal.create(user);
    }

    @Transactional
    public UserDetails loadUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        
        // Load the password from AuthUser table
        AuthUser authUser = authUserRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Auth user not found for email: " + user.getEmail()));
        
        // Set the password in the user object (transient field)
        user.setPassword(authUser.getPasswordHash());

        return UserPrincipal.create(user);
    }
}
