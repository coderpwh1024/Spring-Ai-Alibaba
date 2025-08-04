package com.example.rag.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author coderpwh
 */
public class AgenticWorkflowState {
    
    private String sessionId;
    private String currentTask;
    private int iterationCount;
    private int maxIterations;
    private List<String> executionHistory;
    private Map<String, Object> context;
    private List<AgenticTool> availableTools;
    private boolean completed;
    private String finalResponse;

    public AgenticWorkflowState() {
        this.iterationCount = 0;
        this.maxIterations = 5;
        this.executionHistory = new ArrayList<>();
        this.context = new HashMap<>();
        this.availableTools = new ArrayList<>();
        this.completed = false;
    }

    public AgenticWorkflowState(String sessionId, String currentTask) {
        this();
        this.sessionId = sessionId;
        this.currentTask = currentTask;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getCurrentTask() {
        return currentTask;
    }

    public void setCurrentTask(String currentTask) {
        this.currentTask = currentTask;
    }

    public int getIterationCount() {
        return iterationCount;
    }

    public void setIterationCount(int iterationCount) {
        this.iterationCount = iterationCount;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public List<String> getExecutionHistory() {
        return executionHistory;
    }

    public void setExecutionHistory(List<String> executionHistory) {
        this.executionHistory = executionHistory;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }

    public List<AgenticTool> getAvailableTools() {
        return availableTools;
    }

    public void setAvailableTools(List<AgenticTool> availableTools) {
        this.availableTools = availableTools;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getFinalResponse() {
        return finalResponse;
    }

    public void setFinalResponse(String finalResponse) {
        this.finalResponse = finalResponse;
    }

    public void addToHistory(String entry) {
        this.executionHistory.add(entry);
    }

    public void incrementIteration() {
        this.iterationCount++;
    }

    public boolean hasReachedMaxIterations() {
        return iterationCount >= maxIterations;
    }

    public void addContextData(String key, Object value) {
        this.context.put(key, value);
    }

    public Object getContextData(String key) {
        return this.context.get(key);
    }
}