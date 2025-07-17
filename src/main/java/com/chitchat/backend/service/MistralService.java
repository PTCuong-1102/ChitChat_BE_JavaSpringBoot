package com.chitchat.backend.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Implementation of AIService for Mistral
 */
@Service
public class MistralService implements AIService {

    private static final String PROVIDER_NAME = "mistral";
    private static final String[] SUPPORTED_MODELS = {"mistral-large-latest", "mistral-medium-latest", "mistral-small-latest"};

    @Override
    public CompletableFuture<String> generateResponse(String prompt, String model, String apiKey) {
        // Implement Mistral API call here
        return CompletableFuture.completedFuture("Mistral response for prompt");
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

        promptBuilder.append("You are an AI assistant powered by Mistral. ");
        promptBuilder.append("Please provide informative and engaging responses. ");

        if (conversationContext != null && !conversationContext.trim().isEmpty()) {
            promptBuilder.append("Previous conversation context: ").append(conversationContext).append(" ");
        }

        promptBuilder.append("User's message: ").append(userMessage);

        return promptBuilder.toString();
    }
}
