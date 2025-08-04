package com.example.rag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @author coderpwh
 */
public class QueryRequest {
    
    @NotBlank(message = "Query cannot be empty")
    @Size(max = 2000, message = "Query is too long")
    private String query;
    
    private String sessionId;
    
    private boolean useAgentic = false;

    public QueryRequest() {}

    public QueryRequest(String query, String sessionId) {
        this.query = query;
        this.sessionId = sessionId;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isUseAgentic() {
        return useAgentic;
    }

    public void setUseAgentic(boolean useAgentic) {
        this.useAgentic = useAgentic;
    }
}