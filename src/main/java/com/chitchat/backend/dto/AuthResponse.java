package com.chitchat.backend.dto;

public class AuthResponse {
    
    private String token;
    private String type = "Bearer";
    private UserResponse user;
    
    // Constructors
    public AuthResponse() {}
    
    public AuthResponse(String token, UserResponse user) {
        this.token = token;
        this.user = user;
    }
    
    // Getters and Setters
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public UserResponse getUser() {
        return user;
    }
    
    public void setUser(UserResponse user) {
        this.user = user;
    }
}
