package com.chitchat.backend.exception;

public class ResourceNotFoundException extends ChitChatException {
    
    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND");
    }
    
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue), "RESOURCE_NOT_FOUND");
    }
}
