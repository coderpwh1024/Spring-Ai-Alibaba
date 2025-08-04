package com.example.rag.model;

import java.util.Map;

/**
 * @author coderpwh
 */
public class AgenticTool {
    
    private String name;
    private String description;
    private Map<String, Object> parameters;
    private boolean required;

    public AgenticTool() {}

    public AgenticTool(String name, String description, Map<String, Object> parameters, boolean required) {
        this.name = name;
        this.description = description;
        this.parameters = parameters;
        this.required = required;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}