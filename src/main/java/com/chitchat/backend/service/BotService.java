package com.chitchat.backend.service;

import com.chitchat.backend.entity.Bot;
import com.chitchat.backend.entity.BotIntegration;
import com.chitchat.backend.entity.User;
import com.chitchat.backend.repository.BotRepository;
import com.chitchat.backend.repository.BotIntegrationRepository;
import com.chitchat.backend.repository.UserRepository;
import com.chitchat.backend.exception.ResourceNotFoundException;
import com.chitchat.backend.exception.ChitChatException;
import com.chitchat.backend.dto.BotDto;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
public class BotService {
    
    private static final Logger logger = LoggerFactory.getLogger(BotService.class);
    
    @Autowired
    private BotRepository botRepository;
    
    @Autowired
    private BotIntegrationRepository botIntegrationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AIServiceManager aiServiceManager;
    
    /**
     * Create a new bot with AI integration
     * @param botName The name of the bot
     * @param description The description of the bot
     * @param provider The AI provider (gemini, openai, mistral)
     * @param model The AI model to use
     * @param apiKey The API key for the provider
     * @param creatorId The ID of the user creating the bot
     * @return The created bot
     */
    public CompletableFuture<Bot> createBot(String botName, String description, String provider, String model, String apiKey, UUID creatorId) {
        logger.info("Creating bot '{}' with provider '{}' and model '{}'", botName, provider, model);
        
        // Validate user exists
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + creatorId));
        
        // Check if bot name already exists for this user
        if (botRepository.existsByNameAndCreatorIdAndIsActiveTrue(botName, creatorId)) {
            throw new ChitChatException("Bot with name '" + botName + "' already exists for this user");
        }
        
        // Test the AI provider connection first
        return aiServiceManager.testConnection(provider, apiKey, model)
                .thenCompose(isValid -> {
                    if (!isValid) {
                        throw new ChitChatException("Invalid API key or connection failed for provider: " + provider);
                    }
                    
                    // Create the bot
                    Bot bot = new Bot(botName, description, creatorId);
                    bot.setCreator(creator);
                    Bot savedBot = botRepository.save(bot);
                    
                    // Create the integration
                    BotIntegration integration = new BotIntegration(savedBot.getId(), provider, apiKey, null, model);
                    integration.setBot(savedBot);
                    botIntegrationRepository.save(integration);
                    
                    logger.info("Successfully created bot '{}' with ID: {}", botName, savedBot.getId());
                    return CompletableFuture.completedFuture(savedBot);
                });
    }
    
    /**
     * Get all bots for a user
     * @param userId The user ID
     * @return List of bots
     */
    public List<Bot> getBotsByUser(UUID userId) {
        return botRepository.findByCreatorIdWithIntegrations(userId);
    }
    
    /**
     * Get user's bots for API response
     * @param userId The user ID
     * @return CompletableFuture with List of bots
     */
    public CompletableFuture<List<BotDto>> getUserBots(UUID userId) {
        logger.info("Fetching bots for user ID: {}", userId);
        try {
            List<Bot> bots = botRepository.findByCreatorIdWithIntegrations(userId);
            logger.info("Found {} bots for user {}", bots.size(), userId);
            List<BotDto> botDtos = bots.stream().map(this::convertToDto).collect(Collectors.toList());
            return CompletableFuture.completedFuture(botDtos);
        } catch (Exception e) {
            logger.error("Error fetching bots for user {}: {}", userId, e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    public BotDto convertToDto(Bot bot) {
        BotDto dto = new BotDto();
        dto.setId(bot.getId());
        dto.setName(bot.getName());
        dto.setDescription(bot.getDescription());
        dto.setCreatorId(bot.getCreatorId());
        dto.setAvatarUrl(bot.getAvatarUrl());
        dto.setIsActive(bot.getIsActive());
        dto.setCreatedAt(bot.getCreatedAt());
        dto.setUpdatedAt(bot.getUpdatedAt());
        return dto;
    }
    
    /**
     * Get a bot by ID
     * @param botId The bot ID
     * @return Optional Bot
     */
    public Optional<Bot> getBotById(UUID botId) {
        return botRepository.findById(botId);
    }
    
    /**
     * Update bot configuration
     * @param botId The bot ID
     * @param provider The AI provider
     * @param model The AI model
     * @param apiKey The API key
     * @param userId The user ID (for authorization)
     * @return Updated bot
     */
    public CompletableFuture<Bot> updateBotConfiguration(UUID botId, String provider, String model, String apiKey, UUID userId) {
        logger.info("Updating bot configuration for bot ID: {}", botId);
        
        Bot bot = botRepository.findById(botId)
                .orElseThrow(() -> new ResourceNotFoundException("Bot not found with ID: " + botId));
        
        // Check if user is the creator
        if (!bot.getCreatorId().equals(userId)) {
            throw new ChitChatException("You are not authorized to update this bot");
        }
        
        // Test the new configuration
        return aiServiceManager.testConnection(provider, apiKey, model)
                .thenCompose(isValid -> {
                    if (!isValid) {
                        throw new ChitChatException("Invalid API key or connection failed for provider: " + provider);
                    }
                    
                    // Update or create integration
                    Optional<BotIntegration> existingIntegration = 
                            botIntegrationRepository.findByBotIdAndIntegrationTypeAndIsActiveTrue(botId, provider);
                    
                    if (existingIntegration.isPresent()) {
                        // Update existing integration
                        BotIntegration integration = existingIntegration.get();
                        integration.setApiKey(apiKey);
                        integration.setModel(model);
                        botIntegrationRepository.save(integration);
                    } else {
                        // Create new integration
                        BotIntegration integration = new BotIntegration(botId, provider, apiKey, null, model);
                        integration.setBot(bot);
                        botIntegrationRepository.save(integration);
                    }
                    
                    logger.info("Successfully updated bot configuration for bot ID: {}", botId);
                    return CompletableFuture.completedFuture(bot);
                });
    }
    
    /**
     * Delete a bot
     * @param botId The bot ID
     * @param userId The user ID (for authorization)
     */
    public void deleteBot(UUID botId, UUID userId) {
        logger.info("Deleting bot with ID: {}", botId);
        
        Bot bot = botRepository.findById(botId)
                .orElseThrow(() -> new ResourceNotFoundException("Bot not found with ID: " + botId));
        
        // Check if user is the creator
        if (!bot.getCreatorId().equals(userId)) {
            throw new ChitChatException("You are not authorized to delete this bot");
        }
        
        // Soft delete - set as inactive
        bot.setIsActive(false);
        botRepository.save(bot);
        
        // Deactivate all integrations
        botIntegrationRepository.deactivateAllIntegrationsForBot(botId);
        
        logger.info("Successfully deleted bot with ID: {}", botId);
    }
    
    /**
     * Generate a response using a bot
     * @param botId The bot ID
     * @param prompt The user's message
     * @param userId The user ID (for authorization)
     * @return The AI response
     */
    public CompletableFuture<String> generateBotResponse(UUID botId, String prompt, UUID userId) {
        logger.info("Generating response for bot ID: {}", botId);
        
        Bot bot = botRepository.findById(botId)
                .orElseThrow(() -> new ResourceNotFoundException("Bot not found with ID: " + botId));
        
        // Check if user is the creator
        if (!bot.getCreatorId().equals(userId)) {
            throw new ChitChatException("You are not authorized to use this bot");
        }
        
        // Get the bot's integration
        List<BotIntegration> integrations = botIntegrationRepository.findByBotIdAndIsActiveTrue(botId);
        
        if (integrations.isEmpty()) {
            throw new ChitChatException("No active integrations found for this bot");
        }
        
        // Use the first active integration (in future versions, we might support multiple)
        BotIntegration integration = integrations.get(0);
        
        // Generate response using the appropriate AI service
        return aiServiceManager.generateResponse(
                integration.getIntegrationType(),
                prompt,
                integration.getModel() != null ? integration.getModel() : "default",
                integration.getApiKey()
        );
    }
    
    /**
     * Get bot integration by type
     * @param botId The bot ID
     * @param integrationType The integration type
     * @return Optional BotIntegration
     */
    public Optional<BotIntegration> getBotIntegration(UUID botId, String integrationType) {
        return botIntegrationRepository.findByBotIdAndIntegrationTypeAndIsActiveTrue(botId, integrationType);
    }
}
