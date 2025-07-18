package com.chitchat.backend.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "user_contacts",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "friend_id"})
        })
@EntityListeners(AuditingEntityListener.class)
public class UserContact {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(name = "user_id")
    private UUID userId;
    
    @Column(name = "friend_id")
    private UUID friendId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ContactStatus status = ContactStatus.PENDING;
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    // Relationships
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
    
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id", insertable = false, updatable = false)
    private User friend;
    
    // Constructors
    public UserContact() {}
    
    public UserContact(UUID userId, UUID friendId, ContactStatus status) {
        this.userId = userId;
        this.friendId = friendId;
        this.status = status;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getUserId() {
        return userId;
    }
    
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    
    public UUID getFriendId() {
        return friendId;
    }
    
    public void setFriendId(UUID friendId) {
        this.friendId = friendId;
    }
    
    public ContactStatus getStatus() {
        return status;
    }
    
    public void setStatus(ContactStatus status) {
        this.status = status;
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
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public User getFriend() {
        return friend;
    }
    
    public void setFriend(User friend) {
        this.friend = friend;
    }
    
    // Equals and HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserContact that = (UserContact) o;
        return Objects.equals(id, that.id) && 
               Objects.equals(userId, that.userId) && 
               Objects.equals(friendId, that.friendId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, userId, friendId);
    }
    
    // toString
    @Override
    public String toString() {
        return "UserContact{" +
                "id=" + id +
                ", userId=" + userId +
                ", friendId=" + friendId +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", isActive=" + isActive +
                '}';
    }
}
