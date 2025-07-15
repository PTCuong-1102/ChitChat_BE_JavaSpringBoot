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

import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

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
}
