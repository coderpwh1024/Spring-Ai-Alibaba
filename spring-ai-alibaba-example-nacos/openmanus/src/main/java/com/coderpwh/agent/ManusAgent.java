package com.coderpwh.agent;

import com.coderpwh.llm.LlmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.model.tool.ToolCallingManager;

/**
 * @author coderpwh
 */
public class ManusAgent  extends ToolCallAgent{

    private static final Logger log = LoggerFactory.getLogger(ManusAgent.class);

    private String name = "Manus";

    private String description = "A versatile agent that can solve various tasks using multiple tools";

    public ManusAgent(LlmService llmService, ToolCallingManager toolCallingManager) {
        super(llmService, toolCallingManager);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

}
