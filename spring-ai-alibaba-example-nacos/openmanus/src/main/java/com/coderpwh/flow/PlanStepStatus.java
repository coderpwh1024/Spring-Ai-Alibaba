package com.coderpwh.flow;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author coderpwh
 */

public enum PlanStepStatus {
    NOT_STARTED("not_started"),
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    BLOCKED("blocked");

    private final String value;

    PlanStepStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static List<String> getAllStatuses() {
        // Return a list of all possible step status values
        return Arrays.stream(PlanStepStatus.values()).map(PlanStepStatus::getValue).collect(Collectors.toList());
    }

    public static List<String> getActiveStatuses() {
        // Return a list of values representing active statuses (not started or in
        // progress)
        return Arrays.asList(NOT_STARTED.getValue(), IN_PROGRESS.getValue());
    }

    public static Map<String, String> getStatusMarks() {
        // Return a mapping of statuses to their marker symbols
        return new HashMap<String, String>() {
            {
                put(COMPLETED.getValue(), "[✓]");
                put(IN_PROGRESS.getValue(), "[→]");
                put(BLOCKED.getValue(), "[!]");
                put(NOT_STARTED.getValue(), "[ ]");
            }
        };
    }

    @Override
    public String toString() {
        return value;
    }
}
