package com.chitchat.backend.controller;

import com.chitchat.backend.dto.*;
import com.chitchat.backend.security.UserPrincipal;
import com.chitchat.backend.service.ChatService;
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
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        List<ChatRoomResponse> rooms = chatService.getUserRooms(userPrincipal.getId());
        return ResponseEntity.ok(rooms);
    }

    /**
     * Create a new chat room
     */
    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomResponse> createRoom(@Valid @RequestBody CreateRoomRequest createRoomRequest,
                                                      Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        ChatRoomResponse chatRoomResponse = chatService.createRoom(createRoomRequest, userPrincipal.getId());
        return ResponseEntity.ok(chatRoomResponse);
    }

    /**
     * Send a message to a room
     */
    @PostMapping("/rooms/{roomId}/messages")
    public ResponseEntity<MessageResponse> sendMessage(@PathVariable UUID roomId,
                                                        @Valid @RequestBody SendMessageRequest sendMessageRequest,
                                                        Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        MessageResponse messageResponse = chatService.sendMessage(sendMessageRequest, roomId, userPrincipal.getId());
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
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Page<MessageResponse> messages = chatService.getRoomMessages(roomId, userPrincipal.getId(), page, size);
        return ResponseEntity.ok(messages);
    }

    /**
     * Get room details
     */
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ChatRoomResponse> getRoomDetails(@PathVariable UUID roomId,
                                                           Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        ChatRoomResponse roomDetails = chatService.getRoomDetails(roomId, userPrincipal.getId());
        return ResponseEntity.ok(roomDetails);
    }

    /**
     * Add participant to room
     */
    @PostMapping("/rooms/{roomId}/participants")
    public ResponseEntity<Void> addParticipant(@PathVariable UUID roomId,
                                                @RequestParam UUID participantId,
                                                Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        chatService.addParticipant(roomId, userPrincipal.getId(), participantId);
        return ResponseEntity.ok().build();
    }

    /**
     * Remove participant from room
     */
    @DeleteMapping("/rooms/{roomId}/participants/{participantId}")
    public ResponseEntity<Void> removeParticipant(@PathVariable UUID roomId,
                                                   @PathVariable UUID participantId,
                                                   Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        chatService.removeParticipant(roomId, userPrincipal.getId(), participantId);
        return ResponseEntity.ok().build();
    }
}
