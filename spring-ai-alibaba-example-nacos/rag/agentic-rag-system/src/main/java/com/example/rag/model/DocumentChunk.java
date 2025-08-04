package com.example.rag.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author coderpwh
 */
public class DocumentChunk implements Serializable {
    
    @JsonProperty("chunk_id")
    private String chunkId;
    
    @JsonProperty("document_id")
    private String documentId;
    
    @JsonProperty("content")
    private String content;
    
    @JsonProperty("chunk_index")
    private int chunkIndex;
    
    @JsonProperty("start_position")
    private int startPosition;
    
    @JsonProperty("end_position")
    private int endPosition;
    
    @JsonProperty("embedding")
    private List<Double> embedding;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    public DocumentChunk() {}

    public DocumentChunk(String chunkId, String documentId, String content, int chunkIndex) {
        this.chunkId = chunkId;
        this.documentId = documentId;
        this.content = content;
        this.chunkIndex = chunkIndex;
    }

    public String getChunkId() {
        return chunkId;
    }

    public void setChunkId(String chunkId) {
        this.chunkId = chunkId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(int chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(int startPosition) {
        this.startPosition = startPosition;
    }

    public int getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(int endPosition) {
        this.endPosition = endPosition;
    }

    public List<Double> getEmbedding() {
        return embedding;
    }

    public void setEmbedding(List<Double> embedding) {
        this.embedding = embedding;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}