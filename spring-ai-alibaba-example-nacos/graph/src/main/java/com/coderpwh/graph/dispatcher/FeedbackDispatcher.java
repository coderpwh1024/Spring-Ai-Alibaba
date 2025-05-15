package com.coderpwh.graph.dispatcher;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.EdgeAction;

/**
 * @author coderpwh
 */
public class FeedbackDispatcher implements EdgeAction {
    @Override
    public String apply(OverAllState state) throws Exception {
        String feedback = (String) state.value("summary_feedback").orElse("");
        if (feedback.contains("positive")) {
            return "positive";
        }
        return "negative";
    }

}
