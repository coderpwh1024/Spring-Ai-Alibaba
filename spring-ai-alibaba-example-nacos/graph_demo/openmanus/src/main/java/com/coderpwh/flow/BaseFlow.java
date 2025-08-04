package com.coderpwh.flow;

import com.coderpwh.agent.BaseAgent;

import java.util.Map;

/**
 * @author coderpwh
 */
public abstract class BaseFlow {


    protected Map<String, BaseAgent> agents;

    public BaseFlow(Map<String, BaseAgent> agents, Map<String, Object> data) {
        this.agents = agents;
        data.put("agents", agents);
    }

    public abstract String execute(String inputText);
}
