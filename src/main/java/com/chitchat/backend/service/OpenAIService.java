package com.chitchat.backend.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Implementation of AIService for OpenAI
 */
@Service
public class OpenAIService implements AIService {

    private static final String PROVIDER_NAME = "openai";
    private static final String[] SUPPORTED_MODELS = {"gpt-4o", "gpt-4o-mini", "gpt-4-turbo", "gpt-3.5-turbo"};

    @Override
    public CompletableFuture<String> generateResponse(String prompt, String model, String apiKey) {
        // Implement OpenAI API call here
        return CompletableFuture.completedFuture("OpenAI response for prompt");
    }

    @Override
    public CompletableFuture<String> generateChatResponse(String userMessage, String conversationContext, String model, String apiKey) {
        String prompt = buildChatPrompt(userMessage, conversationContext);
        return generateResponse(prompt, model, apiKey);
    }

    @Override
    public CompletableFuture<Boolean> testConnection(String apiKey, String model) {
        return generateResponse("Test connection", SUPPORTED_MODELS[0], apiKey)
                .thenApply(response -> response != null && !response.isEmpty());
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public String[] getSupportedModels() {
        return SUPPORTED_MODELS;
    }

    private String buildChatPrompt(String userMessage, String conversationContext) {
        StringBuilder promptBuilder = new StringBuilder();

        promptBuilder.append("You are an AI assistant for OpenAI. ");
        promptBuilder.append("Please provide informative and engaging responses. ");

        if (conversationContext != null && !conversationContext.trim().isEmpty()) {
            promptBuilder.append("Previous conversation context: ").append(conversationContext).append(" ");
        }

        promptBuilder.append("User's message: ").append(userMessage);

        return promptBuilder.toString();
    }
}
