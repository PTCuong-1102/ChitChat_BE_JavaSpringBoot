package com.chitchat.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public class CreateRoomRequest {
    
    @NotBlank(message = "Room name is required")
    @Size(min = 1, max = 100, message = "Room name must be between 1 and 100 characters")
    private String name;
    
    @NotNull(message = "Room type is required")
    private Boolean isGroup;
    
    private String description;
    
    private List<UUID> participantIds;
    
    // Constructors
    public CreateRoomRequest() {}
    
    public CreateRoomRequest(String name, Boolean isGroup, String description, List<UUID> participantIds) {
        this.name = name;
        this.isGroup = isGroup;
        this.description = description;
        this.participantIds = participantIds;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Boolean getIsGroup() {
        return isGroup;
    }
    
    public void setIsGroup(Boolean isGroup) {
        this.isGroup = isGroup;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<UUID> getParticipantIds() {
        return participantIds;
    }
    
    public void setParticipantIds(List<UUID> participantIds) {
        this.participantIds = participantIds;
    }
}
