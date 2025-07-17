package com.chitchat.backend.dto;

import com.chitchat.backend.entity.Message;
import com.chitchat.backend.entity.MessageType;

import java.time.LocalDateTime;
import java.util.UUID;

public class MessageResponse {
    
    private UUID id;
    private UUID roomId;
    private UUID senderId;
    private String content;
    private MessageType messageType;
    private LocalDateTime sentAt;
    private LocalDateTime editedAt;
    private UserResponse sender;
    private Boolean isEdited;
    private String roomName;
    
    // Constructors
    public MessageResponse() {}
    
    public MessageResponse(Message message) {
        this.id = message.getId();
        this.roomId = message.getRoomId();
        this.senderId = message.getSenderId();
        this.content = message.getContent();
        this.messageType = message.getMessageType();
        this.sentAt = message.getSentAt();
        this.editedAt = message.getEditedAt();
        this.isEdited = message.getEditedAt() != null;
    }
    
    // Static factory method
    public static MessageResponse fromMessage(Message message) {
        return new MessageResponse(message);
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getRoomId() {
        return roomId;
    }
    
    public void setRoomId(UUID roomId) {
        this.roomId = roomId;
    }
    
    public UUID getSenderId() {
        return senderId;
    }
    
    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public MessageType getMessageType() {
        return messageType;
    }
    
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
    
    public LocalDateTime getSentAt() {
        return sentAt;
    }
    
    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
    
    public LocalDateTime getEditedAt() {
        return editedAt;
    }
    
    public void setEditedAt(LocalDateTime editedAt) {
        this.editedAt = editedAt;
    }
    
    public UserResponse getSender() {
        return sender;
    }
    
    public void setSender(UserResponse sender) {
        this.sender = sender;
    }
    
    public Boolean getIsEdited() {
        return isEdited;
    }
    
    public void setIsEdited(Boolean isEdited) {
        this.isEdited = isEdited;
    }
    
    public String getRoomName() {
        return roomName;
    }
    
    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
}
