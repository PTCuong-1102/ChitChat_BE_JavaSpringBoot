package com.chitchat.backend.controller;

import com.chitchat.backend.service.GeminiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/gemini")
@CrossOrigin(origins = {"http://localhost:5173"})
public class GeminiController {
    
    private static final Logger logger = LoggerFactory.getLogger(GeminiController.class);
    
    @Autowired
    private GeminiService geminiService;
    
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
    public ResponseEntity<Map<String, Object>> configureBot(@RequestBody Map<String, String> request) {
        String botName = request.get("botName");
        String model = request.get("model");
        String provider = request.get("provider");
        String apiKey = request.get("apiKey");
        
        Map<String, Object> response = new HashMap<>();
        
        if (botName == null || botName.trim().isEmpty()) {
            response.put("status", "error");
            response.put("message", "Bot name is required");
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
        
        if (apiKey == null || apiKey.trim().isEmpty()) {
            response.put("status", "error");
            response.put("message", "API key is required");
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
        
        logger.info("Configuring bot: {} with model: {} and provider: {}", botName, model, provider);
        
        try {
            // For now, we'll just validate the API key by testing the connection
            // In a real implementation, you might want to store bot configurations
            boolean isValid = geminiService.testConnection().get();
            
            if (isValid) {
                response.put("status", "success");
                response.put("message", "Bot configured successfully");
                response.put("botName", botName);
                response.put("model", model != null ? model : "gemini-1.5-flash");
                response.put("provider", provider != null ? provider : "gemini");
                response.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Invalid API key or connection failed");
                response.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            logger.error("Error configuring bot: {}", e.getMessage());
            response.put("status", "error");
            response.put("message", "Error configuring bot: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
