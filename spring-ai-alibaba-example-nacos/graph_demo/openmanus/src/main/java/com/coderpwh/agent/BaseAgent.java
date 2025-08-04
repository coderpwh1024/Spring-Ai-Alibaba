package com.coderpwh.agent;

import com.coderpwh.llm.LlmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author coderpwh
 */
public  abstract  class BaseAgent {
    private static final Logger log = LoggerFactory.getLogger(BaseAgent.class);

    private final ReentrantLock lock = new ReentrantLock();

    private String conversationId;

    private String name = "Unique name of the agent";

    private String description = "Optional agent description";

    private String systemPrompt = "Default system-level instruction prompt";

    private String nextStepPrompt = "Default prompt for determining next action";

    private AgentState state = AgentState.IDLE;

    protected LlmService llmService;

    private int maxSteps = 8;

    private int currentStep = 0;

    private Map<String, Object> data = new HashMap<>();

    public BaseAgent(LlmService llmService) {
        this.llmService = llmService;
    }

    public String run(Map<String, Object> data) {
        currentStep = 0;
        if (state != AgentState.IDLE) {
            throw new IllegalStateException("Cannot run agent from state: " + state);
        }

        setData(data);

        List<String> results = new ArrayList<>();
        lock.lock();
        try {
            state = AgentState.RUNNING;
            while (currentStep < maxSteps && !state.equals(AgentState.FINISHED)) {
                currentStep++;
                log.info("Executing round " + currentStep + "/" + maxSteps);
                String stepResult = step();
                if (isStuck()) {
                    handleStuckState();
                }
                results.add("Round " + currentStep + ": " + stepResult);
            }
            if (currentStep >= maxSteps) {
                results.add("Terminated: Reached max rounds (" + maxSteps + ")");
            }
        } finally {
            lock.unlock();
            // Reset state after execution
            state = AgentState.IDLE;
        }
        return String.join("\n", results);
    }

    protected abstract String step();

    private void handleStuckState() {
        String stuckPrompt = "Observed duplicate responses. Consider new strategies and avoid repeating ineffective paths already attempted.";
        nextStepPrompt = stuckPrompt + "\n" + nextStepPrompt;
        log.warn("Agent detected stuck state. Added prompt: " + stuckPrompt);
    }

    /**
     * TODO check stuck status
     *
     * @return whether the agent is stuck
     */
    private boolean isStuck() {
        return false;
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

    public void setState(AgentState state) {
        this.state = state;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    Map<String, Object> getData() {
        return data;
    }

    void setData(Map<String, Object> data) {
        this.data = data;
    }

}
