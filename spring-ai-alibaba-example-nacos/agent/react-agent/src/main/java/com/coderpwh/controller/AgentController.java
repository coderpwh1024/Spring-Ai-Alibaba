package com.coderpwh.controller;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author coderpwh
 */
@Controller
public class AgentController {


    private final ReactAgent reactAgent;


    private final Map<String, InterruptionMetadata> map = new ConcurrentHashMap();

    public AgentController(ReactAgent reactAgent) {
        this.reactAgent = reactAgent;
    }


    public List<InterruptionMetadata.ToolFeedback> invoke(@RequestParam("query") String query,
                                                          @RequestParam("threadId") String threadId) throws Exception {

        RunnableConfig runnableConfig = RunnableConfig.builder().threadId(threadId).build();

        InterruptionMetadata metadata = (InterruptionMetadata) reactAgent.invokeAndGetOutput(query, runnableConfig).orElseThrow();
        map.put(threadId, metadata);
        return metadata.toolFeedbacks();
    }


}
