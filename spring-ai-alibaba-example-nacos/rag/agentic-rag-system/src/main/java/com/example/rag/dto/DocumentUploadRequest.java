package com.example.rag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DocumentUploadRequest {
    
    @NotBlank(message = "Content cannot be empty")
    @Size(max = 1000000, message = "Content is too large")
    private String content;
    
    @Size(max = 255, message = "Title is too long")
    private String title;
    
    @Size(max = 500, message = "Source is too long")
    private String source;

    public DocumentUploadRequest() {}

    public DocumentUploadRequest(String content, String title, String source) {
        this.content = content;
        this.title = title;
        this.source = source;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}