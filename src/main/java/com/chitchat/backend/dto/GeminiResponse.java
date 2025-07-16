package com.chitchat.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class GeminiResponse {
    
    @JsonProperty("candidates")
    private List<Candidate> candidates;
    
    @JsonProperty("usageMetadata")
    private UsageMetadata usageMetadata;
    
    public GeminiResponse() {}
    
    public List<Candidate> getCandidates() {
        return candidates;
    }
    
    public void setCandidates(List<Candidate> candidates) {
        this.candidates = candidates;
    }
    
    public UsageMetadata getUsageMetadata() {
        return usageMetadata;
    }
    
    public void setUsageMetadata(UsageMetadata usageMetadata) {
        this.usageMetadata = usageMetadata;
    }
    
    public static class Candidate {
        @JsonProperty("content")
        private Content content;
        
        @JsonProperty("finishReason")
        private String finishReason;
        
        @JsonProperty("index")
        private Integer index;
        
        public Candidate() {}
        
        public Content getContent() {
            return content;
        }
        
        public void setContent(Content content) {
            this.content = content;
        }
        
        public String getFinishReason() {
            return finishReason;
        }
        
        public void setFinishReason(String finishReason) {
            this.finishReason = finishReason;
        }
        
        public Integer getIndex() {
            return index;
        }
        
        public void setIndex(Integer index) {
            this.index = index;
        }
    }
    
    public static class Content {
        @JsonProperty("parts")
        private List<Part> parts;
        
        @JsonProperty("role")
        private String role;
        
        public Content() {}
        
        public List<Part> getParts() {
            return parts;
        }
        
        public void setParts(List<Part> parts) {
            this.parts = parts;
        }
        
        public String getRole() {
            return role;
        }
        
        public void setRole(String role) {
            this.role = role;
        }
    }
    
    public static class Part {
        @JsonProperty("text")
        private String text;
        
        public Part() {}
        
        public String getText() {
            return text;
        }
        
        public void setText(String text) {
            this.text = text;
        }
    }
    
    public static class UsageMetadata {
        @JsonProperty("promptTokenCount")
        private Integer promptTokenCount;
        
        @JsonProperty("candidatesTokenCount")
        private Integer candidatesTokenCount;
        
        @JsonProperty("totalTokenCount")
        private Integer totalTokenCount;
        
        public UsageMetadata() {}
        
        public Integer getPromptTokenCount() {
            return promptTokenCount;
        }
        
        public void setPromptTokenCount(Integer promptTokenCount) {
            this.promptTokenCount = promptTokenCount;
        }
        
        public Integer getCandidatesTokenCount() {
            return candidatesTokenCount;
        }
        
        public void setCandidatesTokenCount(Integer candidatesTokenCount) {
            this.candidatesTokenCount = candidatesTokenCount;
        }
        
        public Integer getTotalTokenCount() {
            return totalTokenCount;
        }
        
        public void setTotalTokenCount(Integer totalTokenCount) {
            this.totalTokenCount = totalTokenCount;
        }
    }
}
