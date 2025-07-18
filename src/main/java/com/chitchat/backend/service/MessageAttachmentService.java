package com.chitchat.backend.service;

import com.chitchat.backend.entity.Message;
import com.chitchat.backend.entity.MessageAttachment;
import com.chitchat.backend.exception.ChitChatException;
import com.chitchat.backend.exception.ResourceNotFoundException;
import com.chitchat.backend.repository.MessageAttachmentRepository;
import com.chitchat.backend.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MessageAttachmentService {

    private static final Logger logger = LoggerFactory.getLogger(MessageAttachmentService.class);

    @Autowired
    private MessageAttachmentRepository messageAttachmentRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ChatService chatService;

    /**
     * Create a new attachment for a message
     */
    public MessageAttachment createAttachment(UUID messageId, String originalFileName, String storedFileName, 
                                            String fileType, Long fileSize, UUID userId) {
        logger.info("Creating attachment for message: {} by user: {}", messageId, userId);
        
        // Verify message exists and user has access
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + messageId));
        
        // Verify user has access to the message (is participant in the room)
        boolean hasAccess = chatService.isUserParticipantInRoom(message.getRoomId(), userId);
        if (!hasAccess) {
            throw new ChitChatException("User does not have access to this message", "UNAUTHORIZED_ACCESS");
        }
        
        // Create attachment
        MessageAttachment attachment = new MessageAttachment();
        attachment.setMessageId(messageId);
        attachment.setFileName(originalFileName);
        attachment.setFileUrl(storedFileName);  // Store the actual file name for download
        attachment.setFileType(fileType);
        attachment.setFileSize(fileSize);
        attachment.setIsActive(true);
        
        MessageAttachment savedAttachment = messageAttachmentRepository.save(attachment);
        
        logger.info("Attachment created successfully: {}", savedAttachment.getId());
        return savedAttachment;
    }

    /**
     * Get all attachments for a message
     */
    public List<MessageAttachment> getMessageAttachments(UUID messageId) {
        logger.debug("Getting attachments for message: {}", messageId);
        return messageAttachmentRepository.findByMessageIdAndIsActiveTrue(messageId);
    }

    /**
     * Get attachments for multiple messages
     */
    public List<MessageAttachment> getAttachmentsForMessages(List<UUID> messageIds) {
        logger.debug("Getting attachments for {} messages", messageIds.size());
        return messageAttachmentRepository.findByMessageIdsAndIsActiveTrue(messageIds);
    }

    /**
     * Delete an attachment
     */
    public boolean deleteAttachment(UUID attachmentId, UUID userId) {
        logger.info("Deleting attachment: {} by user: {}", attachmentId, userId);
        
        try {
            MessageAttachment attachment = messageAttachmentRepository.findById(attachmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Attachment not found with id: " + attachmentId));
            
            // Verify message exists and user has access
            Message message = messageRepository.findById(attachment.getMessageId())
                    .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
            
            // Check if user is the sender of the message or admin of the room
            boolean canDelete = message.getSenderId().equals(userId) || 
                              chatService.isUserAdminInRoom(message.getRoomId(), userId);
            
            if (!canDelete) {
                throw new ChitChatException("User does not have permission to delete this attachment", "UNAUTHORIZED_DELETE");
            }
            
            // Soft delete the attachment
            attachment.setIsActive(false);
            messageAttachmentRepository.save(attachment);
            
            // Optionally delete the physical file
            try {
                fileStorageService.deleteFile(attachment.getFileUrl());
            } catch (Exception e) {
                logger.warn("Failed to delete physical file: {}", attachment.getFileUrl(), e);
                // Don't fail the operation if file deletion fails
            }
            
            logger.info("Attachment deleted successfully: {}", attachmentId);
            return true;
            
        } catch (ResourceNotFoundException e) {
            logger.error("Failed to delete attachment: {}", attachmentId, e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error deleting attachment: {}", attachmentId, e);
            return false;
        }
    }

    /**
     * Get attachment by ID with access verification
     */
    public MessageAttachment getAttachment(UUID attachmentId, UUID userId) {
        logger.debug("Getting attachment: {} for user: {}", attachmentId, userId);
        
        MessageAttachment attachment = messageAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found with id: " + attachmentId));
        
        if (!attachment.getIsActive()) {
            throw new ResourceNotFoundException("Attachment not found with id: " + attachmentId);
        }
        
        // Verify user has access to the message
        Message message = messageRepository.findById(attachment.getMessageId())
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
        
        boolean hasAccess = chatService.isUserParticipantInRoom(message.getRoomId(), userId);
        if (!hasAccess) {
            throw new ChitChatException("User does not have access to this attachment", "UNAUTHORIZED_ACCESS");
        }
        
        return attachment;
    }

    /**
     * Get total attachment count for a message
     */
    public long getAttachmentCount(UUID messageId) {
        return messageAttachmentRepository.countByMessageIdAndIsActiveTrue(messageId);
    }
}