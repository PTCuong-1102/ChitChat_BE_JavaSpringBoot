package com.chitchat.backend.dto;

import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {
    
    @Size(min = 2, max = 50, message = "Full name must be between 2 and 50 characters")
    private String fullName;
    
    private String avatarUrl;
    
    private Boolean status;
    
    // Constructors
    public UpdateProfileRequest() {}
    
    public UpdateProfileRequest(String fullName, String avatarUrl, Boolean status) {
        this.fullName = fullName;
        this.avatarUrl = avatarUrl;
        this.status = status;
    }
    
    // Getters and Setters
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public Boolean getStatus() {
        return status;
    }
    
    public void setStatus(Boolean status) {
        this.status = status;
    }
}
