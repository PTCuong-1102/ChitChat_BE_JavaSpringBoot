package com.chitchat.backend.dto;

import com.chitchat.backend.entity.ChatRoom;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ChatRoomResponse {
    
    private UUID id;
    private String name;
    private Boolean isGroup;
    private UUID creatorId;
    private String avatarUrl;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<UserResponse> participants;
    private MessageResponse lastMessage;
    private Long participantCount;
    
    // Constructors
    public ChatRoomResponse() {}
    
    public ChatRoomResponse(ChatRoom chatRoom) {
        this.id = chatRoom.getId();
        this.name = chatRoom.getName();
        this.isGroup = chatRoom.getIsGroup();
        this.creatorId = chatRoom.getCreatorId();
        this.avatarUrl = chatRoom.getAvatarUrl();
        this.description = chatRoom.getDescription();
        this.createdAt = chatRoom.getCreatedAt();
        this.updatedAt = chatRoom.getUpdatedAt();
    }
    
    // Static factory method
    public static ChatRoomResponse fromChatRoom(ChatRoom chatRoom) {
        return new ChatRoomResponse(chatRoom);
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
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
    
    public UUID getCreatorId() {
        return creatorId;
    }
    
    public void setCreatorId(UUID creatorId) {
        this.creatorId = creatorId;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<UserResponse> getParticipants() {
        return participants;
    }
    
    public void setParticipants(List<UserResponse> participants) {
        this.participants = participants;
    }
    
    public MessageResponse getLastMessage() {
        return lastMessage;
    }
    
    public void setLastMessage(MessageResponse lastMessage) {
        this.lastMessage = lastMessage;
    }
    
    public Long getParticipantCount() {
        return participantCount;
    }
    
    public void setParticipantCount(Long participantCount) {
        this.participantCount = participantCount;
    }
}
