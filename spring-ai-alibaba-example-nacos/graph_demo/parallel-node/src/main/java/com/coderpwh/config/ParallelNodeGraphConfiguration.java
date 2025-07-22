package com.coderpwh.config;

import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.coderpwh.dispatcher.CollectorDispatcher;
import com.coderpwh.node.CollectorNode;
import com.coderpwh.node.DispatcherNode;
import com.coderpwh.node.ExpanderNode;
import com.coderpwh.node.TranslateNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.alibaba.cloud.ai.graph.StateGraph.END;

import java.util.HashMap;
import java.util.Map;

import static com.alibaba.cloud.ai.graph.action.AsyncEdgeAction.edge_async;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;


/**
 * @author coderpwh
 */
@Configuration
public class ParallelNodeGraphConfiguration {


    private static final Logger logger = LoggerFactory.getLogger(ParallelNodeGraphConfiguration.class);

    @Bean
    public StateGraph parallelNodeGraph(ChatClient.Builder chatClientBuilder) throws GraphStateException {
        KeyStrategyFactory keyStrategyFactory = () -> {
            HashMap<String, KeyStrategy> keyStrategyMap = new HashMap<>();

            keyStrategyMap.put("query", new ReplaceStrategy());
            keyStrategyMap.put("expander_number", new ReplaceStrategy());
            keyStrategyMap.put("expander_content", new ReplaceStrategy());
            keyStrategyMap.put("translate_language", new ReplaceStrategy());
            keyStrategyMap.put("translate_content", new ReplaceStrategy());
            keyStrategyMap.put("collector_next_node", new ReplaceStrategy());

            keyStrategyMap.put("expand_status", new ReplaceStrategy());
            keyStrategyMap.put("translate_status", new ReplaceStrategy());

            return keyStrategyMap;
        };

        StateGraph stateGraph = new StateGraph(keyStrategyFactory)
                .addNode("dispatcher", node_async(new DispatcherNode()))
                .addNode("translator", node_async(new TranslateNode(chatClientBuilder)))
                .addNode("expander", node_async(new ExpanderNode(chatClientBuilder)))
                .addNode("collector", node_async(new CollectorNode()))

                .addEdge("dispatcher", "translator")
                .addEdge("dispatcher", "expander")
                .addEdge("translator", "collector")
                .addEdge("expander", "collector")

                .addEdge(StateGraph.START, "dispatcher")
                .addConditionalEdges("collector", edge_async(new CollectorDispatcher()), Map.of("dispatcher", "dispatcher", END, END));

        GraphRepresentation representation = stateGraph.getGraph(GraphRepresentation.Type.PLANTUML,
                "parallel translator and expander flow");
        logger.info("\n=== Parallel Translator and Expander UML Flow ===");
        logger.info(representation.content());
        logger.info("==================================\n");

        return stateGraph;
    }


}
