package com.coderpwh.config;

import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncCommandAction;
import com.alibaba.cloud.ai.graph.action.AsyncEdgeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.coderpwh.dispatcher.HumanFeedbackDispatcher;
import com.coderpwh.node.ExpanderNode;
import com.coderpwh.node.HumanFeedbackNode;
import com.coderpwh.node.TranslateNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * @author coderpwh
 */
@Configuration
public class GraphHumanConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(GraphHumanConfiguration.class);


    @Bean
    public StateGraph humanGraph(ChatClient.Builder chatClientBuilder) throws GraphStateException {

        KeyStrategyFactory keyStrategyFactory = () -> {
            HashMap<String, KeyStrategy> keyStrategyHashMap = new HashMap<>();

            keyStrategyHashMap.put("query", new ReplaceStrategy());
            keyStrategyHashMap.put("thread_id", new ReplaceStrategy());

            keyStrategyHashMap.put("expander_number", new ReplaceStrategy());
            keyStrategyHashMap.put("expander_content", new ReplaceStrategy());

            keyStrategyHashMap.put("feed_back", new ReplaceStrategy());
            keyStrategyHashMap.put("human_next_node", new ReplaceStrategy());

            keyStrategyHashMap.put("translate_language", new ReplaceStrategy());
            keyStrategyHashMap.put("translate_content", new ReplaceStrategy());

            return keyStrategyHashMap;
        };


        StateGraph stateGraph = new StateGraph(keyStrategyFactory)
                .addNode("expander", node_async(new ExpanderNode(chatClientBuilder)))
                .addNode("translate", node_async(new TranslateNode(chatClientBuilder)))
                .addNode("human_feedback", node_async(new HumanFeedbackNode()))

                .addEdge(StateGraph.START, "expander")
                .addEdge("expander", "human_feedback")
                .addConditionalEdges("human_feedback", AsyncEdgeAction.edge_async((new HumanFeedbackDispatcher())), Map.of(
                        "translate", "translate", StateGraph.END, StateGraph.END))
                .addEdge("translate", StateGraph.END);


        GraphRepresentation representation = stateGraph.getGraph(GraphRepresentation.Type.PLANTUML, "human flow");

        logger.info("\n=== expander UML Flow ===");
        logger.info(representation.content());
        logger.info("==================================\n");

        return stateGraph;
    }


}
