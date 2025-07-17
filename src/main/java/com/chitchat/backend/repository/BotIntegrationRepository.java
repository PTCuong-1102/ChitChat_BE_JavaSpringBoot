package com.chitchat.backend.repository;

import com.chitchat.backend.entity.BotIntegration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BotIntegrationRepository extends JpaRepository<BotIntegration, UUID> {
    
    /**
     * Find all integrations for a specific bot
     * @param botId The ID of the bot
     * @return List of integrations for the bot
     */
    List<BotIntegration> findByBotIdAndIsActiveTrue(UUID botId);
    
    /**
     * Find a specific integration by bot and integration type
     * @param botId The ID of the bot
     * @param integrationType The type of integration (e.g., "gemini", "openai", "mistral")
     * @return Optional BotIntegration if found
     */
    Optional<BotIntegration> findByBotIdAndIntegrationTypeAndIsActiveTrue(UUID botId, String integrationType);
    
    /**
     * Find all active integrations
     * @return List of active integrations
     */
    List<BotIntegration> findByIsActiveTrue();
    
    /**
     * Check if an integration exists for a bot and type
     * @param botId The bot ID
     * @param integrationType The integration type
     * @return true if exists, false otherwise
     */
    boolean existsByBotIdAndIntegrationTypeAndIsActiveTrue(UUID botId, String integrationType);
    
    /**
     * Deactivate all integrations for a bot
     * @param botId The bot ID
     */
    @Modifying
    @Query("UPDATE BotIntegration bi SET bi.isActive = false WHERE bi.botId = :botId")
    void deactivateAllIntegrationsForBot(@Param("botId") UUID botId);
    
    /**
     * Deactivate a specific integration
     * @param botId The bot ID
     * @param integrationType The integration type
     */
    @Modifying
    @Query("UPDATE BotIntegration bi SET bi.isActive = false WHERE bi.botId = :botId AND bi.integrationType = :integrationType")
    void deactivateIntegration(@Param("botId") UUID botId, @Param("integrationType") String integrationType);
}
