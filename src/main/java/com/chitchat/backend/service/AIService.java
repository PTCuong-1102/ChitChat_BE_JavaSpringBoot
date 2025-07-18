package com.chitchat.backend.service;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for AI service providers
 */
public interface AIService {
    
    /**
     * Generate a response using the AI provider
     * @param prompt The user's message/prompt
     * @param model The model to use (provider-specific)
     * @param apiKey The API key for the provider
     * @return CompletableFuture containing the AI response
     */
    CompletableFuture<String> generateResponse(String prompt, String model, String apiKey);
    
    /**
     * Generate a conversational response for chat messages
     * @param userMessage The user's message
     * @param conversationContext Optional context from previous messages
     * @param model The model to use (provider-specific)
     * @param apiKey The API key for the provider
     * @return CompletableFuture containing the AI response
     */
    CompletableFuture<String> generateChatResponse(String userMessage, String conversationContext, String model, String apiKey);
    
    /**
     * Test the AI provider connection
     * @param apiKey The API key for the provider
     * @param model The model to test (optional)
     * @return CompletableFuture containing true if successful, false otherwise
     */
    CompletableFuture<Boolean> testConnection(String apiKey, String model);
    
    /**
     * Get the provider name
     * @return The name of the AI provider
     */
    String getProviderName();
    
    /**
     * Get supported models for this provider
     * @return Array of supported model names
     */
    String[] getSupportedModels();
}
