package com.chitchat.backend.controller;

import com.chitchat.backend.dto.*;
import com.chitchat.backend.security.UserPrincipal;
import com.chitchat.backend.service.ChatService;
import com.chitchat.backend.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ChatController {

    @Autowired
    private ChatService chatService;

    /**
     * Get all rooms for current user
     */
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomResponse>> getUserRooms(Authentication authentication) {
        UUID userId = SecurityUtils.getUserId(authentication);
        List<ChatRoomResponse> rooms = chatService.getUserRooms(userId);
        return ResponseEntity.ok(rooms);
    }

    /**
     * Create a new chat room
     */
    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomResponse> createRoom(@Valid @RequestBody CreateRoomRequest createRoomRequest,
                                                      Authentication authentication) {
        UUID userId = SecurityUtils.getUserId(authentication);
        ChatRoomResponse chatRoomResponse = chatService.createRoom(createRoomRequest, userId);
        return ResponseEntity.ok(chatRoomResponse);
    }

    /**
     * Send a message to a room
     */
    @PostMapping("/rooms/{roomId}/messages")
    public ResponseEntity<MessageResponse> sendMessage(@PathVariable UUID roomId,
                                                        @Valid @RequestBody SendMessageRequest sendMessageRequest,
                                                        Authentication authentication) {
        UUID userId = SecurityUtils.getUserId(authentication);
        MessageResponse messageResponse = chatService.sendMessage(sendMessageRequest, roomId, userId);
        return ResponseEntity.ok(messageResponse);
    }

    /**
     * Get messages for a room with pagination
     */
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<Page<MessageResponse>> getRoomMessages(@PathVariable UUID roomId,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "20") int size,
                                                                 Authentication authentication) {
        UUID userId = SecurityUtils.getUserId(authentication);
        Page<MessageResponse> messages = chatService.getRoomMessages(roomId, userId, page, size);
        return ResponseEntity.ok(messages);
    }

    /**
     * Get room details
     */
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ChatRoomResponse> getRoomDetails(@PathVariable UUID roomId,
                                                           Authentication authentication) {
        UUID userId = SecurityUtils.getUserId(authentication);
        ChatRoomResponse roomDetails = chatService.getRoomDetails(roomId, userId);
        return ResponseEntity.ok(roomDetails);
    }

    /**
     * Add participant to room
     */
    @PostMapping("/rooms/{roomId}/participants")
    public ResponseEntity<Void> addParticipant(@PathVariable UUID roomId,
                                                @RequestParam UUID participantId,
                                                Authentication authentication) {
        UUID userId = SecurityUtils.getUserId(authentication);
        chatService.addParticipant(roomId, userId, participantId);
        return ResponseEntity.ok().build();
    }

    /**
     * Remove participant from room
     */
    @DeleteMapping("/rooms/{roomId}/participants/{participantId}")
    public ResponseEntity<Void> removeParticipant(@PathVariable UUID roomId,
                                                   @PathVariable UUID participantId,
                                                   Authentication authentication) {
        UUID userId = SecurityUtils.getUserId(authentication);
        chatService.removeParticipant(roomId, userId, participantId);
        return ResponseEntity.ok().build();
    }

    /**
     * Edit message
     */
    @PutMapping("/messages/{messageId}")
    public ResponseEntity<MessageResponse> editMessage(@PathVariable UUID messageId,
                                                      @Valid @RequestBody SendMessageRequest editRequest,
                                                      Authentication authentication) {
        UUID userId = SecurityUtils.getUserId(authentication);
        MessageResponse editedMessage = chatService.editMessage(messageId, editRequest, userId);
        return ResponseEntity.ok(editedMessage);
    }

    /**
     * Delete message
     */
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable UUID messageId,
                                              Authentication authentication) {
        UUID userId = SecurityUtils.getUserId(authentication);
        chatService.deleteMessage(messageId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Search messages in a specific room
     */
    @GetMapping("/rooms/{roomId}/messages/search")
    public ResponseEntity<Page<MessageResponse>> searchMessagesInRoom(@PathVariable UUID roomId,
                                                                     @RequestParam String query,
                                                                     @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "20") int size,
                                                                     Authentication authentication) {
        UUID userId = SecurityUtils.getUserId(authentication);
        Page<MessageResponse> searchResults = chatService.searchMessagesInRoom(roomId, query, userId, page, size);
        return ResponseEntity.ok(searchResults);
    }

    /**
     * Search messages across all user's rooms
     */
    @GetMapping("/messages/search")
    public ResponseEntity<Page<MessageResponse>> searchMessagesGlobal(@RequestParam String query,
                                                                     @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "20") int size,
                                                                     Authentication authentication) {
        UUID userId = SecurityUtils.getUserId(authentication);
        Page<MessageResponse> searchResults = chatService.searchMessagesAcrossUserRooms(userId, query, page, size);
        return ResponseEntity.ok(searchResults);
    }

    /**
     * Find or create direct message room with another user
     */
    @PostMapping("/rooms/dm/{friendId}")
    public ResponseEntity<ChatRoomResponse> findOrCreateDirectMessageRoom(
            @PathVariable UUID friendId,
            Authentication authentication) {
        UUID userId = SecurityUtils.getUserId(authentication);
        ChatRoomResponse dmRoom = chatService.findOrCreateDirectMessageRoom(userId, friendId);
        return ResponseEntity.ok(dmRoom);
    }
}
