package com.chitchat.backend.controller;

import com.chitchat.backend.dto.MessageResponse;
import com.chitchat.backend.dto.SendMessageRequest;
import com.chitchat.backend.entity.MessageType;
import com.chitchat.backend.security.UserPrincipal;
import com.chitchat.backend.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
public class WebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    @Autowired
    private ChatService chatService;

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    /**
     * Handle sending message through WebSocket
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload SendMessageWithRoomRequest sendMessageRequest,
                           SimpMessageHeaderAccessor headerAccessor,
                           Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            
            // Create SendMessageRequest without roomId
            SendMessageRequest messageRequest = new SendMessageRequest();
            messageRequest.setContent(sendMessageRequest.getContent());
            messageRequest.setMessageType(sendMessageRequest.getMessageType());
            
            // Send message through service
            MessageResponse messageResponse = chatService.sendMessage(messageRequest, sendMessageRequest.getRoomId(), userPrincipal.getId());
            
            // Broadcast message to room subscribers
            messagingTemplate.convertAndSend("/topic/room/" + sendMessageRequest.getRoomId(), messageResponse);
            
            logger.debug("Message sent via WebSocket: {}", messageResponse.getId());
        } catch (Exception e) {
            logger.error("Error sending message via WebSocket", e);
            // Send error message back to user
            messagingTemplate.convertAndSendToUser(
                authentication.getName(),
                "/queue/errors",
                "Failed to send message: " + e.getMessage()
            );
        }
    }

    /**
     * Handle user joining a room
     */
    @MessageMapping("/chat.joinRoom")
    public void joinRoom(@Payload JoinRoomMessage joinRoomMessage,
                        SimpMessageHeaderAccessor headerAccessor,
                        Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            
            // Add user to session
            headerAccessor.getSessionAttributes().put("username", userPrincipal.getUsername());
            headerAccessor.getSessionAttributes().put("roomId", joinRoomMessage.getRoomId());
            
            // Notify room about user joining
            RoomEventMessage roomEvent = new RoomEventMessage();
            roomEvent.setType("USER_JOINED");
            roomEvent.setUserId(userPrincipal.getId());
            roomEvent.setUsername(userPrincipal.getUsername());
            roomEvent.setRoomId(joinRoomMessage.getRoomId());
            roomEvent.setMessage(userPrincipal.getUsername() + " joined the room");
            
            messagingTemplate.convertAndSend("/topic/room/" + joinRoomMessage.getRoomId() + "/events", roomEvent);
            
            logger.debug("User {} joined room {}", userPrincipal.getUsername(), joinRoomMessage.getRoomId());
        } catch (Exception e) {
            logger.error("Error joining room via WebSocket", e);
        }
    }

    /**
     * Handle user leaving a room
     */
    @MessageMapping("/chat.leaveRoom")
    public void leaveRoom(@Payload LeaveRoomMessage leaveRoomMessage,
                         SimpMessageHeaderAccessor headerAccessor,
                         Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            
            // Notify room about user leaving
            RoomEventMessage roomEvent = new RoomEventMessage();
            roomEvent.setType("USER_LEFT");
            roomEvent.setUserId(userPrincipal.getId());
            roomEvent.setUsername(userPrincipal.getUsername());
            roomEvent.setRoomId(leaveRoomMessage.getRoomId());
            roomEvent.setMessage(userPrincipal.getUsername() + " left the room");
            
            messagingTemplate.convertAndSend("/topic/room/" + leaveRoomMessage.getRoomId() + "/events", roomEvent);
            
            logger.debug("User {} left room {}", userPrincipal.getUsername(), leaveRoomMessage.getRoomId());
        } catch (Exception e) {
            logger.error("Error leaving room via WebSocket", e);
        }
    }

    /**
     * Handle typing indicators
     */
    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload TypingMessage typingMessage,
                            Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            
            typingMessage.setUserId(userPrincipal.getId());
            typingMessage.setUsername(userPrincipal.getUsername());
            
            // Send typing indicator to room
            messagingTemplate.convertAndSend("/topic/room/" + typingMessage.getRoomId() + "/typing", typingMessage);
            
            logger.debug("Typing indicator from user {} in room {}", userPrincipal.getUsername(), typingMessage.getRoomId());
        } catch (Exception e) {
            logger.error("Error handling typing indicator", e);
        }
    }

    // Inner classes for WebSocket messages
    public static class SendMessageWithRoomRequest {
        private String content;
        private MessageType messageType = MessageType.TEXT;
        private UUID roomId;
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public MessageType getMessageType() { return messageType; }
        public void setMessageType(MessageType messageType) { this.messageType = messageType; }
        
        public UUID getRoomId() { return roomId; }
        public void setRoomId(UUID roomId) { this.roomId = roomId; }
    }
    
    public static class JoinRoomMessage {
        private UUID roomId;
        
        public UUID getRoomId() { return roomId; }
        public void setRoomId(UUID roomId) { this.roomId = roomId; }
    }

    public static class LeaveRoomMessage {
        private UUID roomId;
        
        public UUID getRoomId() { return roomId; }
        public void setRoomId(UUID roomId) { this.roomId = roomId; }
    }

    public static class TypingMessage {
        private UUID roomId;
        private UUID userId;
        private String username;
        private boolean isTyping;
        
        public UUID getRoomId() { return roomId; }
        public void setRoomId(UUID roomId) { this.roomId = roomId; }
        
        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public boolean isTyping() { return isTyping; }
        public void setTyping(boolean typing) { isTyping = typing; }
    }

    public static class RoomEventMessage {
        private String type;
        private UUID userId;
        private String username;
        private UUID roomId;
        private String message;
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public UUID getRoomId() { return roomId; }
        public void setRoomId(UUID roomId) { this.roomId = roomId; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
