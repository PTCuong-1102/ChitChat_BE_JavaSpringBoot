package com.chitchat.backend.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "room_participants",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"room_id", "user_id"})
        })
@EntityListeners(AuditingEntityListener.class)
public class RoomParticipant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(name = "room_id")
    private UUID roomId;
    
    @Column(name = "user_id")
    private UUID userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private ParticipantRole role = ParticipantRole.MEMBER;
    
    @CreatedDate
    @Column(name = "joined_at", updatable = false)
    private LocalDateTime joinedAt;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", insertable = false, updatable = false)
    private ChatRoom chatRoom;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
    
    // Constructors
    public RoomParticipant() {}
    
    public RoomParticipant(UUID roomId, UUID userId, ParticipantRole role) {
        this.roomId = roomId;
        this.userId = userId;
        this.role = role;
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
    
    public UUID getUserId() {
        return userId;
    }
    
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    
    public ParticipantRole getRole() {
        return role;
    }
    
    public void setRole(ParticipantRole role) {
        this.role = role;
    }
    
    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }
    
    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public ChatRoom getChatRoom() {
        return chatRoom;
    }
    
    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    // Equals and HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoomParticipant that = (RoomParticipant) o;
        return Objects.equals(id, that.id) && 
               Objects.equals(roomId, that.roomId) && 
               Objects.equals(userId, that.userId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, roomId, userId);
    }
    
    // toString
    @Override
    public String toString() {
        return "RoomParticipant{" +
                "id=" + id +
                ", roomId=" + roomId +
                ", userId=" + userId +
                ", role=" + role +
                ", joinedAt=" + joinedAt +
                ", isActive=" + isActive +
                '}';
    }
}
