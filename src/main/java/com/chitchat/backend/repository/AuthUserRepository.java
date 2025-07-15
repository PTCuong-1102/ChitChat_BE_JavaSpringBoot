package com.chitchat.backend.repository;

import com.chitchat.backend.entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthUserRepository extends JpaRepository<AuthUser, UUID> {
    
    Optional<AuthUser> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    void deleteByEmail(String email);
}
