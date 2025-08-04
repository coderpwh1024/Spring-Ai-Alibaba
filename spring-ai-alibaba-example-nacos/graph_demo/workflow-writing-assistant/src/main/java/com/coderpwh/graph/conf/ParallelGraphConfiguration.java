package com.coderpwh.graph.conf;

import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.alibaba.cloud.ai.graph.StateGraph.START;
import static com.alibaba.cloud.ai.graph.StateGraph.END;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * @author coderpwh
 */
@Configuration
public class ParallelGraphConfiguration {


    @Bean
    public StateGraph parallelGraph(ChatModel chatModel) throws GraphStateException {

        ChatClient client = ChatClient.builder(chatModel).defaultAdvisors(new SimpleLoggerAdvisor()).build();


        KeyStrategyFactory keyStrategyFactory = () -> {
            HashMap<String, KeyStrategy> keyStrategyHashMap = new HashMap<>();
            keyStrategyHashMap.put("inputText", new ReplaceStrategy());
            keyStrategyHashMap.put("sentiment", new ReplaceStrategy());
            keyStrategyHashMap.put("keywords", new ReplaceStrategy());
            keyStrategyHashMap.put("analysis", new ReplaceStrategy());
            return keyStrategyHashMap;
        };


        StateGraph stateGraph = new StateGraph("ParallelDemo",keyStrategyFactory)
                .addNode("start", node_async(new InputNode()))
                .addNode("sentiment", node_async(new SentimentAnalysisNode(client, "inputText")))
                .addNode("keyword", node_async(new KeywordExtractionNode(client, "inputText")))
                .addNode("merge", node_async(new MergeResultsNode()))

                .addEdge(START, "sentiment")
                .addEdge(START, "keyword")
                .addEdge("sentiment", "merge")
                .addEdge("keyword", "merge")

                .addEdge("merge", END);

        GraphRepresentation representation = stateGraph.getGraph(GraphRepresentation.Type.PLANTUML, "Parallel Graph");
        System.out.println("\n=== Parallel Demo UML Flow ===");
        System.out.println(representation.content());
        System.out.println("==================================\n");

        return stateGraph;
    }


    static class InputNode implements NodeAction {
        @Override
        public Map<String, Object> apply(OverAllState state) throws Exception {
            String text = (String) state.value("inputText").orElse("");
            return Map.of("inputText", text);
        }
    }

    static class SentimentAnalysisNode implements NodeAction {

        private final ChatClient chatClient;

        private final String key;

        public SentimentAnalysisNode(ChatClient chatClient, String key) {
            this.chatClient = chatClient;
            this.key = key;
        }

        @Override
        public Map<String, Object> apply(OverAllState state) throws Exception {
            String text = (String) state.value(key).orElse("");

            ChatResponse resp = chatClient.prompt().user("emotion analysis from: " + text).call().chatResponse();
            String sentiment = resp.getResult().getOutput().getText();
            return Map.of("sentiment", sentiment);
        }
    }


    static class KeywordExtractionNode implements NodeAction {

        private final ChatClient chatClient;

        private final String key;


        public KeywordExtractionNode(ChatClient chatClient, String key) {
            this.chatClient = chatClient;
            this.key = key;
        }

        @Override
        public Map<String, Object> apply(OverAllState state) throws Exception {
            String text = (String) state.value(key).orElse("");
            ChatResponse response = chatClient.prompt().user("extract keywords from: " + text).call().chatResponse();
            String keywords = response.getResult().getOutput().getText();
            return Map.of("keywords", keywords);
        }

    }

    static class MergeResultsNode implements NodeAction {
        @Override
        public Map<String, Object> apply(OverAllState state) throws Exception {
            String sent = (String) state.value("sentiment").orElse("unknown");
            List<?> kws = (List<?>) state.value("keywords").orElse(List.of());
            return Map.of("analysis", Map.of("sentiment", sent, "keywords", kws));
        }


    }


}
