package com.chitchat.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "gemini")
public class GeminiConfig {
    
    private String apiKey;
    private String apiUrl;
    private String model;
    
    // Getters and setters
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public String getApiUrl() {
        return apiUrl;
    }
    
    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
}
