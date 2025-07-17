package com.chitchat.backend.controller;

import com.chitchat.backend.service.GeminiService;
import com.chitchat.backend.service.AIServiceManager;
import com.chitchat.backend.service.BotService;
import com.chitchat.backend.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/gemini")
@CrossOrigin(origins = {"http://localhost:5173"})
public class GeminiController {
    
    private static final Logger logger = LoggerFactory.getLogger(GeminiController.class);
    
    @Autowired
    private GeminiService geminiService;
    
    @Autowired
    private AIServiceManager aiServiceManager;
    
    @Autowired
    private BotService botService;
    
    /**
     * Test Gemini API connection
     */
    @GetMapping("/test")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> testGeminiConnection() {
        logger.info("Testing Gemini API connection");
        
        return geminiService.testConnection()
                .thenApply(isWorking -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", isWorking ? "success" : "failed");
                    response.put("message", isWorking ? 
                        "Gemini API is working correctly" : 
                        "Gemini API connection failed");
                    response.put("timestamp", System.currentTimeMillis());
                    
                    return ResponseEntity.ok(response);
                })
                .exceptionally(throwable -> {
                    logger.error("Error testing Gemini connection: {}", throwable.getMessage());
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "error");
                    response.put("message", "Error testing Gemini API: " + throwable.getMessage());
                    response.put("timestamp", System.currentTimeMillis());
                    
                    return ResponseEntity.internalServerError().body(response);
                });
    }
    
    /**
     * Generate a response using Gemini API
     */
    @PostMapping("/generate")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> generateResponse(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        
        if (prompt == null || prompt.trim().isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Prompt is required");
            response.put("timestamp", System.currentTimeMillis());
            
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(response));
        }
        
        logger.info("Generating response for prompt: {}", prompt);
        
        return geminiService.generateResponse(prompt)
                .thenApply(aiResponse -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "success");
                    response.put("response", aiResponse);
                    response.put("timestamp", System.currentTimeMillis());
                    
                    return ResponseEntity.ok(response);
                })
                .exceptionally(throwable -> {
                    logger.error("Error generating response: {}", throwable.getMessage());
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "error");
                    response.put("message", "Error generating response: " + throwable.getMessage());
                    response.put("timestamp", System.currentTimeMillis());
                    
                    return ResponseEntity.internalServerError().body(response);
                });
    }
    
    /**
     * Generate a chat response with context
     */
    @PostMapping("/chat")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> generateChatResponse(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        String context = request.get("context");
        
        if (userMessage == null || userMessage.trim().isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Message is required");
            response.put("timestamp", System.currentTimeMillis());
            
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(response));
        }
        
        logger.info("Generating chat response for message: {}", userMessage);
        
        return geminiService.generateChatResponse(userMessage, context)
                .thenApply(aiResponse -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "success");
                    response.put("response", aiResponse);
                    response.put("timestamp", System.currentTimeMillis());
                    
                    return ResponseEntity.ok(response);
                })
                .exceptionally(throwable -> {
                    logger.error("Error generating chat response: {}", throwable.getMessage());
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "error");
                    response.put("message", "Error generating chat response: " + throwable.getMessage());
                    response.put("timestamp", System.currentTimeMillis());
                    
                    return ResponseEntity.internalServerError().body(response);
                });
    }
    
    /**
     * Configure AI bot with API key and settings
     */
    @PostMapping("/configure")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> configureBot(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        String botName = request.get("botName");
        String model = request.get("model");
        String provider = request.get("provider");
        String apiKey = request.get("apiKey");
        
        Map<String, Object> response = new HashMap<>();
        
        // Validation
        if (botName == null || botName.trim().isEmpty()) {
            response.put("status", "error");
            response.put("message", "Bot name is required");
            response.put("timestamp", System.currentTimeMillis());
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(response));
        }
        
        if (apiKey == null || apiKey.trim().isEmpty()) {
            response.put("status", "error");
            response.put("message", "API key is required");
            response.put("timestamp", System.currentTimeMillis());
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(response));
        }
        
        if (provider == null || provider.trim().isEmpty()) {
            response.put("status", "error");
            response.put("message", "Provider is required");
            response.put("timestamp", System.currentTimeMillis());
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(response));
        }
        
        if (model == null || model.trim().isEmpty()) {
            response.put("status", "error");
            response.put("message", "Model is required");
            response.put("timestamp", System.currentTimeMillis());
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(response));
        }
        
        // Check if provider is supported
        if (!aiServiceManager.isProviderSupported(provider)) {
            response.put("status", "error");
            response.put("message", "Unsupported provider: " + provider);
            response.put("timestamp", System.currentTimeMillis());
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(response));
        }
        
        logger.info("Configuring bot: {} with model: {} and provider: {}", botName, model, provider);
        
        UUID userId = userPrincipal.getId();
        
        // Create bot using BotService
        return botService.createBot(botName, "AI Bot created via configuration", provider, model, apiKey, userId)
                .thenApply(bot -> {
                    Map<String, Object> successResponse = new HashMap<>();
                    successResponse.put("status", "success");
                    successResponse.put("message", "Bot configured successfully");
                    successResponse.put("botId", bot.getId());
                    successResponse.put("botName", bot.getName());
                    successResponse.put("model", model);
                    successResponse.put("provider", provider);
                    successResponse.put("timestamp", System.currentTimeMillis());
                    
                    return ResponseEntity.ok(successResponse);
                })
                .exceptionally(throwable -> {
                    logger.error("Error configuring bot: {}", throwable.getMessage());
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("status", "error");
                    errorResponse.put("message", "Error configuring bot: " + throwable.getMessage());
                    errorResponse.put("timestamp", System.currentTimeMillis());
                    
                    return ResponseEntity.internalServerError().body(errorResponse);
                });
    }
    
    /**
     * Get user's configured bots
     */
    @GetMapping("/bots")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> getUserBots(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        logger.info("Fetching bots for user: {}", userPrincipal.getId());
        
        UUID userId = userPrincipal.getId();
        
        return botService.getUserBots(userId)
                .thenApply(botDtos -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "success");
                    response.put("bots", botDtos);
                    response.put("count", botDtos.size());
                    response.put("timestamp", System.currentTimeMillis());
                    return ResponseEntity.ok(response);
                })
                .exceptionally(throwable -> {
                    logger.error("Error fetching user bots: {}", throwable.getMessage());
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "error");
                    response.put("message", "Error fetching bots: " + throwable.getMessage());
                    response.put("timestamp", System.currentTimeMillis());
                    return ResponseEntity.internalServerError().body(response);
                });
    }
    
    /**
     * Generate a response using a specific bot
     */
    @PostMapping("/bot/{botId}/response")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> generateBotResponse(
            @PathVariable UUID botId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        String prompt = request.get("prompt");
        
        if (prompt == null || prompt.trim().isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Prompt is required");
            response.put("timestamp", System.currentTimeMillis());
            
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(response));
        }
        
        logger.info("Generating response for bot: {} with prompt: {}", botId, prompt);
        
        UUID userId = userPrincipal.getId();
        
        // Generate response using BotService
        return botService.generateBotResponse(botId, prompt, userId)
                .thenApply(aiResponse -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "success");
                    response.put("response", aiResponse);
                    response.put("botId", botId);
                    response.put("timestamp", System.currentTimeMillis());
                    
                    return ResponseEntity.ok(response);
                })
                .exceptionally(throwable -> {
                    logger.error("Error generating bot response: {}", throwable.getMessage());
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "error");
                    response.put("message", "Error generating bot response: " + throwable.getMessage());
                    response.put("timestamp", System.currentTimeMillis());
                    
                    return ResponseEntity.internalServerError().body(response);
                });
    }
}
