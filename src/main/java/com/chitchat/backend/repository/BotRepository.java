package com.chitchat.backend.repository;

import com.chitchat.backend.entity.Bot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BotRepository extends JpaRepository<Bot, UUID> {
    
    /**
     * Find all bots created by a specific user
     * @param creatorId The ID of the user who created the bots
     * @return List of bots created by the user
     */
    List<Bot> findByCreatorIdAndIsActiveTrue(UUID creatorId);
    
    /**
     * Find a bot by name and creator
     * @param name The name of the bot
     * @param creatorId The ID of the creator
     * @return Optional Bot if found
     */
    Optional<Bot> findByNameAndCreatorIdAndIsActiveTrue(String name, UUID creatorId);
    
    /**
     * Find all active bots
     * @return List of active bots
     */
    List<Bot> findByIsActiveTrue();
    
    /**
     * Check if a bot name exists for a specific creator
     * @param name The bot name to check
     * @param creatorId The creator ID
     * @return true if the name exists, false otherwise
     */
    boolean existsByNameAndCreatorIdAndIsActiveTrue(String name, UUID creatorId);
    
    /**
     * Find bots with their integrations
     * @param creatorId The creator ID
     * @return List of bots with integrations
     */
    @Query("SELECT b FROM Bot b LEFT JOIN FETCH b.integrations WHERE b.creatorId = :creatorId AND b.isActive = true")
    List<Bot> findByCreatorIdWithIntegrations(@Param("creatorId") UUID creatorId);
}
