package com.chitchat.backend.service;

import com.chitchat.backend.dto.*;
import com.chitchat.backend.entity.*;
import com.chitchat.backend.exception.ResourceNotFoundException;
import com.chitchat.backend.exception.UnauthorizedException;
import com.chitchat.backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;
import java.util.Optional;
import java.util.Arrays;

@Service
@Transactional
public class ChatService {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    
    @Autowired
    private ChatRoomRepository chatRoomRepository;
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private RoomParticipantRepository roomParticipantRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Get all rooms for a user
     */
    public List<ChatRoomResponse> getUserRooms(UUID userId) {
        logger.debug("Getting rooms for user: {}", userId);
        
        List<ChatRoom> rooms = chatRoomRepository.findRoomsByUserId(userId);
        
        return rooms.stream()
                .map(room -> {
                    ChatRoomResponse response = ChatRoomResponse.fromChatRoom(room);
                    response.setParticipantCount(roomParticipantRepository.countParticipantsByRoomId(room.getId()));
                    return response;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Create a new chat room
     */
    public ChatRoomResponse createRoom(CreateRoomRequest request, UUID creatorId) {
        logger.info("Creating new room: {} by user: {}", request.getName(), creatorId);
        
        ChatRoom room = new ChatRoom();
        room.setName(request.getName());
        room.setIsGroup(request.getIsGroup());
        room.setCreatorId(creatorId);
        room.setDescription(request.getDescription());
        
        ChatRoom savedRoom = chatRoomRepository.save(room);
        
        // Add creator as admin participant
        RoomParticipant creatorParticipant = new RoomParticipant();
        creatorParticipant.setRoomId(savedRoom.getId());
        creatorParticipant.setUserId(creatorId);
        creatorParticipant.setRole(ParticipantRole.ADMIN);
        roomParticipantRepository.save(creatorParticipant);
        
        // Add other participants if provided
        if (request.getParticipantIds() != null && !request.getParticipantIds().isEmpty()) {
            for (UUID participantId : request.getParticipantIds()) {
                if (!participantId.equals(creatorId)) {
                    RoomParticipant participant = new RoomParticipant();
                    participant.setRoomId(savedRoom.getId());
                    participant.setUserId(participantId);
                    participant.setRole(ParticipantRole.MEMBER);
                    roomParticipantRepository.save(participant);
                }
            }
        }
        
        logger.info("Room created successfully: {}", savedRoom.getId());
        return ChatRoomResponse.fromChatRoom(savedRoom);
    }
    
    /**
     * Send a message to a room
     */
    public MessageResponse sendMessage(SendMessageRequest request, UUID roomId, UUID senderId) {
        logger.debug("Sending message to room: {} by user: {}", roomId, senderId);
        
        // Verify user has access to room
        if (!hasUserAccessToRoom(senderId, roomId)) {
            throw new UnauthorizedException("User does not have access to this room");
        }
        
        Message message = new Message();
        message.setRoomId(roomId);
        message.setSenderId(senderId);
        message.setContent(request.getContent());
        message.setMessageType(request.getMessageType());
        
        Message savedMessage = messageRepository.save(message);
        
        MessageResponse response = MessageResponse.fromMessage(savedMessage);
        
        // Add sender info
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", senderId));
        response.setSender(UserResponse.fromUser(sender));
        
        logger.debug("Message sent successfully: {}", savedMessage.getId());
        return response;
    }
    
    /**
     * Get messages for a room with pagination
     */
    public Page<MessageResponse> getRoomMessages(UUID roomId, UUID userId, int page, int size) {
        logger.debug("Getting messages for room: {} by user: {}", roomId, userId);
        
        // Verify user has access to room
        if (!hasUserAccessToRoom(userId, roomId)) {
            throw new UnauthorizedException("User does not have access to this room");
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAt").descending());
        Page<Message> messages = messageRepository.findByRoomIdOrderBySentAtDesc(roomId, pageable);
        
        return messages.map(message -> {
            MessageResponse response = MessageResponse.fromMessage(message);
            // Add sender info
            User sender = userRepository.findById(message.getSenderId()).orElse(null);
            if (sender != null) {
                response.setSender(UserResponse.fromUser(sender));
            }
            return response;
        });
    }
    
    /**
     * Get room details
     */
    public ChatRoomResponse getRoomDetails(UUID roomId, UUID userId) {
        logger.debug("Getting room details: {} for user: {}", roomId, userId);
        
        // Verify user has access to room
        if (!hasUserAccessToRoom(userId, roomId)) {
            throw new UnauthorizedException("User does not have access to this room");
        }
        
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatRoom", "id", roomId));
        
        ChatRoomResponse response = ChatRoomResponse.fromChatRoom(room);
        
        // Add participants
        List<RoomParticipant> participants = roomParticipantRepository.findByRoomIdAndIsActiveTrue(roomId);
        List<UserResponse> participantUsers = participants.stream()
                .map(participant -> {
                    User user = userRepository.findById(participant.getUserId()).orElse(null);
                    return user != null ? UserResponse.fromUser(user) : null;
                })
                .filter(user -> user != null)
                .collect(Collectors.toList());
        
        response.setParticipants(participantUsers);
        response.setParticipantCount((long) participantUsers.size());
        
        return response;
    }
    
    /**
     * Check if user has access to a room
     */
    private boolean hasUserAccessToRoom(UUID userId, UUID roomId) {
        return roomParticipantRepository.findByRoomIdAndUserIdAndIsActiveTrue(roomId, userId).isPresent();
    }
    
    /**
     * Add participant to room
     */
    public void addParticipant(UUID roomId, UUID userId, UUID participantId) {
        logger.info("Adding participant {} to room {} by user {}", participantId, roomId, userId);
        
        // Verify user is admin of the room
        RoomParticipant userParticipant = roomParticipantRepository.findByRoomIdAndUserIdAndIsActiveTrue(roomId, userId)
                .orElseThrow(() -> new UnauthorizedException("User does not have access to this room"));
        
        if (userParticipant.getRole() != ParticipantRole.ADMIN) {
            throw new UnauthorizedException("Only admins can add participants");
        }
        
        // Check if participant already exists
        if (roomParticipantRepository.findByRoomIdAndUserIdAndIsActiveTrue(roomId, participantId).isPresent()) {
            throw new IllegalArgumentException("User is already a participant in this room");
        }
        
        RoomParticipant participant = new RoomParticipant();
        participant.setRoomId(roomId);
        participant.setUserId(participantId);
        participant.setRole(ParticipantRole.MEMBER);
        
        roomParticipantRepository.save(participant);
        
        logger.info("Participant added successfully");
    }
    
    /**
     * Remove participant from room
     */
    public void removeParticipant(UUID roomId, UUID userId, UUID participantId) {
        logger.info("Removing participant {} from room {} by user {}", participantId, roomId, userId);
        
        // Verify user is admin of the room
        RoomParticipant userParticipant = roomParticipantRepository.findByRoomIdAndUserIdAndIsActiveTrue(roomId, userId)
                .orElseThrow(() -> new UnauthorizedException("User does not have access to this room"));
        
        if (userParticipant.getRole() != ParticipantRole.ADMIN) {
            throw new UnauthorizedException("Only admins can remove participants");
        }
        
        RoomParticipant participant = roomParticipantRepository.findByRoomIdAndUserIdAndIsActiveTrue(roomId, participantId)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found in room"));
        
        participant.setIsActive(false);
        roomParticipantRepository.save(participant);
        
        logger.info("Participant removed successfully");
    }
    
    /**
     * Check if user is a participant in a room
     */
    public boolean isUserParticipantInRoom(UUID roomId, UUID userId) {
        logger.debug("Checking if user {} is participant in room {}", userId, roomId);
        return roomParticipantRepository.findByRoomIdAndUserIdAndIsActiveTrue(roomId, userId).isPresent();
    }
    
    /**
     * Check if user is an admin in a room
     */
    public boolean isUserAdminInRoom(UUID roomId, UUID userId) {
        logger.debug("Checking if user {} is admin in room {}", userId, roomId);
        return roomParticipantRepository.findByRoomIdAndUserIdAndIsActiveTrue(roomId, userId)
                .map(participant -> participant.getRole() == ParticipantRole.ADMIN)
                .orElse(false);
    }

    /**
     * Edit a message
     */
    public MessageResponse editMessage(UUID messageId, SendMessageRequest editRequest, UUID userId) {
        logger.info("Editing message {} by user {}", messageId, userId);
        
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + messageId));
        
        // Check if user is the sender of the message
        if (!message.getSenderId().equals(userId)) {
            throw new UnauthorizedException("User can only edit their own messages");
        }
        
        // Check if message is not too old (e.g., 24 hours)
        if (message.getSentAt().isBefore(LocalDateTime.now().minusHours(24))) {
            throw new IllegalArgumentException("Cannot edit messages older than 24 hours");
        }
        
        // Update message content
        message.setContent(editRequest.getContent());
        message.setEditedAt(LocalDateTime.now());
        
        Message savedMessage = messageRepository.save(message);
        
        // Convert to response
        MessageResponse response = MessageResponse.fromMessage(savedMessage);
        
        // Add sender info
        User sender = userRepository.findById(savedMessage.getSenderId()).orElse(null);
        if (sender != null) {
            response.setSender(UserResponse.fromUser(sender));
        }
        
        logger.info("Message edited successfully: {}", messageId);
        return response;
    }

    /**
     * Delete a message
     */
    public void deleteMessage(UUID messageId, UUID userId) {
        logger.info("Deleting message {} by user {}", messageId, userId);
        
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + messageId));
        
        // Check if user is the sender or admin in the room
        boolean canDelete = message.getSenderId().equals(userId) || 
                          isUserAdminInRoom(message.getRoomId(), userId);
        
        if (!canDelete) {
            throw new UnauthorizedException("User does not have permission to delete this message");
        }
        
        // Soft delete the message
        message.setIsActive(false);
        messageRepository.save(message);
        
        logger.info("Message deleted successfully: {}", messageId);
    }

    /**
     * Search messages in a specific room
     */
    public Page<MessageResponse> searchMessagesInRoom(UUID roomId, String query, UUID userId, int page, int size) {
        logger.debug("Searching messages in room: {} with query: '{}' by user: {}", roomId, query, userId);
        
        // Verify user has access to room
        if (!hasUserAccessToRoom(userId, roomId)) {
            throw new UnauthorizedException("User does not have access to this room");
        }
        
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAt").descending());
        Page<Message> messages = messageRepository.searchMessagesInRoom(roomId, query.trim(), pageable);
        
        return messages.map(message -> {
            MessageResponse response = MessageResponse.fromMessage(message);
            // Add sender info
            User sender = userRepository.findById(message.getSenderId()).orElse(null);
            if (sender != null) {
                response.setSender(UserResponse.fromUser(sender));
            }
            return response;
        });
    }

    /**
     * Search messages across all user's rooms
     */
    public Page<MessageResponse> searchMessagesAcrossUserRooms(UUID userId, String query, int page, int size) {
        logger.debug("Searching messages across all rooms with query: '{}' by user: {}", query, userId);
        
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAt").descending());
        Page<Message> messages = messageRepository.searchMessagesAcrossUserRooms(userId, query.trim(), pageable);
        
        return messages.map(message -> {
            MessageResponse response = MessageResponse.fromMessage(message);
            // Add sender info
            User sender = userRepository.findById(message.getSenderId()).orElse(null);
            if (sender != null) {
                response.setSender(UserResponse.fromUser(sender));
            }
            // Add room info for context
            ChatRoom room = chatRoomRepository.findById(message.getRoomId()).orElse(null);
            if (room != null) {
                response.setRoomName(room.getName());
                response.setRoomId(room.getId());
            }
            return response;
        });
    }

    /**
     * Find or create a direct message room between two users
     */
    public ChatRoomResponse findOrCreateDirectMessageRoom(UUID userId1, UUID userId2) {
        logger.info("Finding or creating DM room between users: {} and {}", userId1, userId2);
        
        // Check if both users exist and are active
        User user1 = userRepository.findById(userId1)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId1));
        User user2 = userRepository.findById(userId2)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId2));
        
        if (!user1.getStatus() || !user2.getStatus()) {
            throw new IllegalStateException("One or both users are not active");
        }
        
        // Try to find existing DM room
        Optional<ChatRoom> existingRoom = chatRoomRepository.findDirectMessageRoom(userId1, userId2);
        
        if (existingRoom.isPresent()) {
            logger.info("Found existing DM room: {}", existingRoom.get().getId());
            ChatRoomResponse response = ChatRoomResponse.fromChatRoom(existingRoom.get());
            
            // Add participants info
            List<RoomParticipant> participants = roomParticipantRepository.findByRoomIdAndIsActiveTrue(existingRoom.get().getId());
            List<UserResponse> participantUsers = participants.stream()
                    .map(participant -> {
                        User user = userRepository.findById(participant.getUserId()).orElse(null);
                        return user != null ? UserResponse.fromUser(user) : null;
                    })
                    .filter(user -> user != null)
                    .collect(Collectors.toList());
            
            response.setParticipants(participantUsers);
            response.setParticipantCount((long) participantUsers.size());
            
            return response;
        }
        
        // Create new DM room
        ChatRoom newRoom = new ChatRoom();
        newRoom.setName("Direct Message"); // This will be overridden by frontend display logic
        newRoom.setIsGroup(false);
        newRoom.setCreatorId(userId1); // The person who initiated the chat
        
        ChatRoom savedRoom = chatRoomRepository.save(newRoom);
        
        // Add both users as participants
        RoomParticipant participant1 = new RoomParticipant();
        participant1.setRoomId(savedRoom.getId());
        participant1.setUserId(userId1);
        participant1.setRole(ParticipantRole.MEMBER);
        roomParticipantRepository.save(participant1);
        
        RoomParticipant participant2 = new RoomParticipant();
        participant2.setRoomId(savedRoom.getId());
        participant2.setUserId(userId2);
        participant2.setRole(ParticipantRole.MEMBER);
        roomParticipantRepository.save(participant2);
        
        logger.info("Created new DM room: {} between users: {} and {}", savedRoom.getId(), userId1, userId2);
        
        // Prepare response
        ChatRoomResponse response = ChatRoomResponse.fromChatRoom(savedRoom);
        
        // Add participants info
        List<UserResponse> participantUsers = Arrays.asList(
            UserResponse.fromUser(user1),
            UserResponse.fromUser(user2)
        );
        response.setParticipants(participantUsers);
        response.setParticipantCount(2L);
        
        return response;
    }
}
