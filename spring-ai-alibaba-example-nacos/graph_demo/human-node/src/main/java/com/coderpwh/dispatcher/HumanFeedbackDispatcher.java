package com.coderpwh.dispatcher;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.EdgeAction;

/**
 * @author coderpwh
 */
public class HumanFeedbackDispatcher implements EdgeAction {

    @Override
    public String apply(OverAllState state) throws Exception {
        return (String) state.value("human_next_node", StateGraph.END);
    }

}
