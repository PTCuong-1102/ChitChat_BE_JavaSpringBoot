package com.chitchat.backend.service;

import com.chitchat.backend.exception.ChitChatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    
    private final Path fileStorageLocation;
    
    // Maximum file size: 50MB
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;
    
    // Allowed file types
    private static final String[] ALLOWED_EXTENSIONS = {
        "jpg", "jpeg", "png", "gif", "pdf", "doc", "docx", "txt", "zip", "rar"
    };

    public FileStorageService(@Value("${file.upload-dir:uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        
        try {
            Files.createDirectories(this.fileStorageLocation);
            logger.info("File storage directory created: {}", this.fileStorageLocation);
        } catch (Exception ex) {
            throw new ChitChatException("Could not create the directory where the uploaded files will be stored.", "FILE_STORAGE_INIT_ERROR");
        }
    }

    public String storeFile(MultipartFile file) {
        // Validate file
        validateFile(file);
        
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        
        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new ChitChatException("Filename contains invalid path sequence: " + fileName, "INVALID_FILENAME");
            }
            
            // Generate unique filename
            String fileExtension = getFileExtension(fileName);
            String uniqueFileName = UUID.randomUUID().toString() + "." + fileExtension;
            
            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            logger.info("File stored successfully: {}", uniqueFileName);
            return uniqueFileName;
            
        } catch (IOException ex) {
            logger.error("Failed to store file: {}", fileName, ex);
            throw new ChitChatException("Could not store file " + fileName + ". Please try again!", "FILE_STORAGE_ERROR");
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                return resource;
            } else {
                throw new ChitChatException("File not found: " + fileName, "FILE_NOT_FOUND");
            }
        } catch (MalformedURLException ex) {
            logger.error("File not found: {}", fileName, ex);
            throw new ChitChatException("File not found: " + fileName, "FILE_NOT_FOUND");
        }
    }

    public boolean deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            boolean deleted = Files.deleteIfExists(filePath);
            
            if (deleted) {
                logger.info("File deleted successfully: {}", fileName);
            } else {
                logger.warn("File not found for deletion: {}", fileName);
            }
            
            return deleted;
        } catch (IOException ex) {
            logger.error("Failed to delete file: {}", fileName, ex);
            return false;
        }
    }

    private void validateFile(MultipartFile file) {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new ChitChatException("Cannot upload empty file", "EMPTY_FILE");
        }
        
        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ChitChatException("File size exceeds maximum limit of " + (MAX_FILE_SIZE / 1024 / 1024) + "MB", "FILE_TOO_LARGE");
        }
        
        // Check file extension
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(fileName).toLowerCase();
        
        boolean isAllowed = false;
        for (String allowedExt : ALLOWED_EXTENSIONS) {
            if (allowedExt.equals(fileExtension)) {
                isAllowed = true;
                break;
            }
        }
        
        if (!isAllowed) {
            throw new ChitChatException("File type not allowed: " + fileExtension, "INVALID_FILE_TYPE");
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    public String getFileStoragePath() {
        return this.fileStorageLocation.toString();
    }
}