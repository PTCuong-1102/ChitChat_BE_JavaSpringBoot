package com.chitchat.backend.service;

import com.chitchat.backend.config.GeminiConfig;
import com.chitchat.backend.dto.GeminiRequest;
import com.chitchat.backend.dto.GeminiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@Service
public class GeminiService implements AIService {
    
    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);
    
    private final WebClient webClient;
    private final GeminiConfig geminiConfig;
    
    @Autowired
    public GeminiService(GeminiConfig geminiConfig) {
        this.geminiConfig = geminiConfig;
        this.webClient = WebClient.builder().build();
    }
    
    /**
     * Generate a response using Gemini API
     * @param prompt The user's message/prompt
     * @return CompletableFuture containing the AI response
     */
    public CompletableFuture<String> generateResponse(String prompt) {
        logger.debug("Generating response for prompt: {}", prompt);
        
        try {
            // Create request
            GeminiRequest.Part part = new GeminiRequest.Part(prompt);
            GeminiRequest.Content content = new GeminiRequest.Content(Arrays.asList(part));
            GeminiRequest request = new GeminiRequest(Arrays.asList(content));
            
            // Make API call
            Mono<GeminiResponse> responseMono = webClient
                    .post()
                    .uri(geminiConfig.getApiUrl() + "?key=" + geminiConfig.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GeminiResponse.class);
            
            // Convert to CompletableFuture
            return responseMono
                    .map(this::extractTextFromResponse)
                    .doOnSuccess(response -> logger.debug("Successfully generated response: {}", response))
                    .doOnError(error -> logger.error("Error generating response: {}", error.getMessage(), error))
                    .toFuture();
                    
        } catch (Exception e) {
            logger.error("Error creating Gemini request: {}", e.getMessage(), e);
            CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    /**
     * Extract text from Gemini response
     * @param response The Gemini API response
     * @return Extracted text content
     */
    private String extractTextFromResponse(GeminiResponse response) {
        if (response == null || response.getCandidates() == null || response.getCandidates().isEmpty()) {
            logger.warn("Empty response from Gemini API");
            return "I apologize, but I'm unable to generate a response right now. Please try again.";
        }
        
        GeminiResponse.Candidate candidate = response.getCandidates().get(0);
        if (candidate.getContent() == null || candidate.getContent().getParts() == null || 
            candidate.getContent().getParts().isEmpty()) {
            logger.warn("No content in Gemini response");
            return "I apologize, but I'm unable to generate a response right now. Please try again.";
        }
        
        GeminiResponse.Part part = candidate.getContent().getParts().get(0);
        String text = part.getText();
        
        if (text == null || text.trim().isEmpty()) {
            logger.warn("Empty text in Gemini response");
            return "I apologize, but I'm unable to generate a response right now. Please try again.";
        }
        
        return text.trim();
    }
    
    /**
     * Generate a conversational response for chat messages
     * @param userMessage The user's message
     * @param conversationContext Optional context from previous messages
     * @return CompletableFuture containing the AI response
     */
    public CompletableFuture<String> generateChatResponse(String userMessage, String conversationContext) {
        String prompt = buildChatPrompt(userMessage, conversationContext);
        return generateResponse(prompt);
    }
    
    /**
     * Build a chat prompt with context
     * @param userMessage The user's message
     * @param conversationContext Previous conversation context
     * @return Formatted prompt for the AI
     */
    private String buildChatPrompt(String userMessage, String conversationContext) {
        StringBuilder promptBuilder = new StringBuilder();
        
        promptBuilder.append("You are a helpful and friendly AI assistant in a chat application. ");
        promptBuilder.append("Please respond in a conversational manner, being helpful and engaging. ");
        promptBuilder.append("Keep responses concise but informative. ");
        
        if (conversationContext != null && !conversationContext.trim().isEmpty()) {
            promptBuilder.append("Previous conversation context: ").append(conversationContext).append(" ");
        }
        
        promptBuilder.append("User's message: ").append(userMessage);
        
        return promptBuilder.toString();
    }
    
    /**
     * Test the Gemini API connection
     * @return CompletableFuture containing true if successful, false otherwise
     */
    public CompletableFuture<Boolean> testConnection() {
        logger.info("Testing Gemini API connection");
        
        return generateResponse("Hello, this is a test message.")
                .thenApply(response -> {
                    boolean isWorking = response != null && !response.contains("unable to generate");
                    logger.info("Gemini API connection test: {}", isWorking ? "SUCCESS" : "FAILED");
                    return isWorking;
                })
                .exceptionally(throwable -> {
                    logger.error("Gemini API connection test failed: {}", throwable.getMessage());
                    return false;
                });
    }
    
    // AIService interface implementation
    
    @Override
    public CompletableFuture<String> generateResponse(String prompt, String model, String apiKey) {
        logger.debug("Generating response for prompt: {} with model: {}", prompt, model);
        
        try {
            // Create request
            GeminiRequest.Part part = new GeminiRequest.Part(prompt);
            GeminiRequest.Content content = new GeminiRequest.Content(Arrays.asList(part));
            GeminiRequest request = new GeminiRequest(Arrays.asList(content));
            
            // Make API call with provided apiKey and model
            String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent";
            
            Mono<GeminiResponse> responseMono = webClient
                    .post()
                    .uri(apiUrl + "?key=" + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GeminiResponse.class);
            
            // Convert to CompletableFuture
            return responseMono
                    .map(this::extractTextFromResponse)
                    .doOnSuccess(response -> logger.debug("Successfully generated response: {}", response))
                    .doOnError(error -> logger.error("Error generating response: {}", error.getMessage(), error))
                    .toFuture();
                    
        } catch (Exception e) {
            logger.error("Error creating Gemini request: {}", e.getMessage(), e);
            CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Override
    public CompletableFuture<String> generateChatResponse(String userMessage, String conversationContext, String model, String apiKey) {
        String prompt = buildChatPrompt(userMessage, conversationContext);
        return generateResponse(prompt, model, apiKey);
    }
    
    @Override
    public CompletableFuture<Boolean> testConnection(String apiKey, String model) {
        logger.info("Testing Gemini API connection with provided apiKey and model: {}", model);
        
        String testModel = (model != null && !model.isEmpty()) ? model : "gemini-1.5-flash";
        
        return generateResponse("Hello, this is a test message.", testModel, apiKey)
                .thenApply(response -> {
                    boolean isWorking = response != null && !response.contains("unable to generate");
                    logger.info("Gemini API connection test: {}", isWorking ? "SUCCESS" : "FAILED");
                    return isWorking;
                })
                .exceptionally(throwable -> {
                    logger.error("Gemini API connection test failed: {}", throwable.getMessage());
                    return false;
                });
    }
    
    @Override
    public String getProviderName() {
        return "gemini";
    }
    
    @Override
    public String[] getSupportedModels() {
        return new String[]{"gemini-1.5-flash", "gemini-1.5-pro"};
    }
}
