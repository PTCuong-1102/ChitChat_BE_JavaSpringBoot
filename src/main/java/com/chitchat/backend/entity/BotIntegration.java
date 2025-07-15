package com.chitchat.backend.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "bot_integrations")
@EntityListeners(AuditingEntityListener.class)
public class BotIntegration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(name = "bot_id")
    private UUID botId;
    
    @Column(name = "integration_type")
    private String integrationType;
    
    @Column(name = "api_key")
    private String apiKey;
    
    @Column(name = "api_secret")
    private String apiSecret;
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bot_id", insertable = false, updatable = false)
    private Bot bot;
    
    // Constructors
    public BotIntegration() {}
    
    public BotIntegration(UUID botId, String integrationType, String apiKey, String apiSecret) {
        this.botId = botId;
        this.integrationType = integrationType;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getBotId() {
        return botId;
    }
    
    public void setBotId(UUID botId) {
        this.botId = botId;
    }
    
    public String getIntegrationType() {
        return integrationType;
    }
    
    public void setIntegrationType(String integrationType) {
        this.integrationType = integrationType;
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public String getApiSecret() {
        return apiSecret;
    }
    
    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Bot getBot() {
        return bot;
    }
    
    public void setBot(Bot bot) {
        this.bot = bot;
    }
    
    // Equals and HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BotIntegration that = (BotIntegration) o;
        return Objects.equals(id, that.id) && 
               Objects.equals(botId, that.botId) && 
               Objects.equals(integrationType, that.integrationType);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, botId, integrationType);
    }
    
    // toString
    @Override
    public String toString() {
        return "BotIntegration{" +
                "id=" + id +
                ", botId=" + botId +
                ", integrationType='" + integrationType + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
