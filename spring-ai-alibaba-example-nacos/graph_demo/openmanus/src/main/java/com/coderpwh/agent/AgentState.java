package com.coderpwh.agent;

/**
 * @author coderpwh
 */

public enum AgentState {

    IDLE("IDLE"), RUNNING("RUNNING"), FINISHED("FINISHED"), ERROR("ERROR");

    private final String state;

    AgentState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return state;
    }

}
