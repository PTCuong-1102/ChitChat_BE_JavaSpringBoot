package com.chitchat.backend.repository;

import com.chitchat.backend.entity.MessageAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageAttachmentRepository extends JpaRepository<MessageAttachment, UUID> {

    @Query("SELECT ma FROM MessageAttachment ma WHERE ma.messageId = :messageId AND ma.isActive = true")
    List<MessageAttachment> findByMessageIdAndIsActiveTrue(@Param("messageId") UUID messageId);

    @Query("SELECT ma FROM MessageAttachment ma WHERE ma.messageId IN :messageIds AND ma.isActive = true")
    List<MessageAttachment> findByMessageIdsAndIsActiveTrue(@Param("messageIds") List<UUID> messageIds);

    @Query("SELECT ma FROM MessageAttachment ma WHERE ma.fileName = :fileName AND ma.isActive = true")
    List<MessageAttachment> findByFileNameAndIsActiveTrue(@Param("fileName") String fileName);

    @Query("SELECT COUNT(ma) FROM MessageAttachment ma WHERE ma.messageId = :messageId AND ma.isActive = true")
    long countByMessageIdAndIsActiveTrue(@Param("messageId") UUID messageId);
}