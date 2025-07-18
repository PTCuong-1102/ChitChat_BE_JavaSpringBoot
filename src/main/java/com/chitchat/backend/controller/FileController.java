package com.chitchat.backend.controller;

import com.chitchat.backend.entity.MessageAttachment;
import com.chitchat.backend.service.FileStorageService;
import com.chitchat.backend.service.MessageAttachmentService;
import com.chitchat.backend.security.UserPrincipal;
import com.chitchat.backend.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private MessageAttachmentService messageAttachmentService;

    /**
     * Upload file for a message
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file,
                                                         @RequestParam("messageId") UUID messageId,
                                                         Authentication authentication) {
        try {
            UUID userId = SecurityUtils.getUserId(authentication);
            
            // Store file
            String fileName = fileStorageService.storeFile(file);
            
            // Create attachment record
            MessageAttachment attachment = messageAttachmentService.createAttachment(
                messageId, 
                file.getOriginalFilename(), 
                fileName, 
                file.getContentType(), 
                file.getSize(),
                userId
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", attachment.getId());
            response.put("fileName", attachment.getFileName());
            response.put("fileUrl", "/api/files/download/" + fileName);
            response.put("fileType", attachment.getFileType());
            response.put("fileSize", attachment.getFileSize());
            response.put("uploadedAt", attachment.getUploadedAt());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception ex) {
            logger.error("File upload failed", ex);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "File upload failed: " + ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Download file
     */
    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, 
                                               HttpServletRequest request,
                                               Authentication authentication) {
        try {
            // Load file as Resource
            Resource resource = fileStorageService.loadFileAsResource(fileName);
            
            // Try to determine file's content type
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                logger.info("Could not determine file type.");
            }
            
            // Fallback to the default content type if type could not be determined
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
                    
        } catch (Exception ex) {
            logger.error("File download failed for: {}", fileName, ex);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get attachments for a message
     */
    @GetMapping("/message/{messageId}")
    public ResponseEntity<List<Map<String, Object>>> getMessageAttachments(@PathVariable UUID messageId,
                                                                          Authentication authentication) {
        try {
            List<MessageAttachment> attachments = messageAttachmentService.getMessageAttachments(messageId);
            
            List<Map<String, Object>> response = attachments.stream()
                .map(attachment -> {
                    Map<String, Object> attachmentData = new HashMap<>();
                    attachmentData.put("id", attachment.getId());
                    attachmentData.put("fileName", attachment.getFileName());
                    attachmentData.put("fileUrl", "/api/files/download/" + attachment.getFileUrl());
                    attachmentData.put("fileType", attachment.getFileType());
                    attachmentData.put("fileSize", attachment.getFileSize());
                    attachmentData.put("uploadedAt", attachment.getUploadedAt());
                    return attachmentData;
                })
                .toList();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception ex) {
            logger.error("Failed to get message attachments for messageId: {}", messageId, ex);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete attachment
     */
    @DeleteMapping("/attachment/{attachmentId}")
    public ResponseEntity<Map<String, String>> deleteAttachment(@PathVariable UUID attachmentId,
                                                               Authentication authentication) {
        try {
            UUID userId = SecurityUtils.getUserId(authentication);
            
            boolean deleted = messageAttachmentService.deleteAttachment(attachmentId, userId);
            
            Map<String, String> response = new HashMap<>();
            if (deleted) {
                response.put("message", "Attachment deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Attachment not found or unauthorized");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception ex) {
            logger.error("Failed to delete attachment: {}", attachmentId, ex);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete attachment: " + ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}