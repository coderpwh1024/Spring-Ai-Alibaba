package com.example.rag.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * @author coderpwh
 */
@Entity
@Table(name = "conversation_messages")
public class ConversationMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private MessageRole role;
    
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "tool_calls", columnDefinition = "JSON")
    private String toolCalls;
    
    @Column(name = "retrieved_documents", columnDefinition = "JSON")
    private String retrievedDocuments;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public ConversationMessage() {
        this.createdAt = LocalDateTime.now();
    }

    public ConversationMessage(Conversation conversation, MessageRole role, String content) {
        this();
        this.conversation = conversation;
        this.role = role;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public MessageRole getRole() {
        return role;
    }

    public void setRole(MessageRole role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getToolCalls() {
        return toolCalls;
    }

    public void setToolCalls(String toolCalls) {
        this.toolCalls = toolCalls;
    }

    public String getRetrievedDocuments() {
        return retrievedDocuments;
    }

    public void setRetrievedDocuments(String retrievedDocuments) {
        this.retrievedDocuments = retrievedDocuments;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public enum MessageRole {
        USER, ASSISTANT, SYSTEM, TOOL
    }
}