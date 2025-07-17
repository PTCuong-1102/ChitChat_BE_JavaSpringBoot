package com.chitchat.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Service
public class AIServiceManager {
    private final Map<String, AIService> aiServiceProviders = new HashMap<>();

    @Autowired
    public AIServiceManager(Set<AIService> aiServices) {
        for (AIService service : aiServices) {
            aiServiceProviders.put(service.getProviderName(), service);
        }
    }

    public CompletableFuture<String> generateResponse(String provider, String prompt, String model, String apiKey) {
        AIService service = aiServiceProviders.get(provider);
        if (service == null) {
            return CompletableFuture.completedFuture("Unsupported provider");
        }
        return service.generateResponse(prompt, model, apiKey);
    }

    public CompletableFuture<String> generateChatResponse(String provider, String userMessage, String conversationContext, String model, String apiKey) {
        AIService service = aiServiceProviders.get(provider);
        if (service == null) {
            return CompletableFuture.completedFuture("Unsupported provider");
        }
        return service.generateChatResponse(userMessage, conversationContext, model, apiKey);
    }

    public CompletableFuture<Boolean> testConnection(String provider, String apiKey, String model) {
        AIService service = aiServiceProviders.get(provider);
        if (service == null) {
            return CompletableFuture.completedFuture(false);
        }
        return service.testConnection(apiKey, model);
    }

    public String[] getSupportedModels(String provider) {
        AIService service = aiServiceProviders.get(provider);
        if (service == null) {
            return new String[]{};
        }
        return service.getSupportedModels();
    }

    public boolean isProviderSupported(String provider) {
        return aiServiceProviders.containsKey(provider);
    }
}

