package com.coderpwh.agent;

import com.coderpwh.llm.LlmService;

/**
 * @author coderpwh
 */
public abstract class ReActAgent extends BaseAgent{

    public ReActAgent(LlmService llmService) {
        super(llmService);
    }

    protected abstract boolean think();

    protected abstract String act();

    @Override
    public String step() {
        boolean shouldAct = think();
        if (!shouldAct) {
            return "Thinking complete - no action needed";
        }
        return act();
    }

}
