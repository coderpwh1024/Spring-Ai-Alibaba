package com.coderpwh.config;

import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.coderpwh.node.ExpanderNode;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * @author coderpwh
 */
@Configuration
public class GraphNodeStreamConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(GraphNodeStreamConfiguration.class);


    @Bean
    public StateGraph streamGraph(ChatClient.Builder chatClientBuilder) throws GraphStateException {

        KeyStrategyFactory keyStrategyFactory = () -> {
            HashMap<String, KeyStrategy> keyStrategyHashMap = new HashMap<>();

            keyStrategyHashMap.put("query", new ReplaceStrategy());
            keyStrategyHashMap.put("expander_number", new ReplaceStrategy());
            keyStrategyHashMap.put("expander_content", new ReplaceStrategy());

            return keyStrategyHashMap;
        };

        StateGraph stateGraph = new StateGraph(keyStrategyFactory)
                .addNode("expander", node_async(new ExpanderNode(chatClientBuilder)))
                .addEdge(StateGraph.START, "expander")
                .addEdge("expander", StateGraph.END);


        GraphRepresentation representation = stateGraph.getGraph(GraphRepresentation.Type.PLANTUML, "expander flow");
        logger.info("\n=== expander UML Flow ===");
        logger.info(representation.content());
        logger.info("==================================\n");

        return  stateGraph;


    }


}
