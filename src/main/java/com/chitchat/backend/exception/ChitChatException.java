package com.chitchat.backend.exception;

public class ChitChatException extends RuntimeException {
    
    private String errorCode;
    
    public ChitChatException(String message) {
        super(message);
    }
    
    public ChitChatException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ChitChatException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public ChitChatException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}
