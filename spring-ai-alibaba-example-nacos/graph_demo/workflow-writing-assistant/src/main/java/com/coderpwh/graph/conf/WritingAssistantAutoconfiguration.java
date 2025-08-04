package com.coderpwh.graph.conf;

import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.EdgeAction;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.StateGraph.START;

import java.util.HashMap;
import java.util.Map;

import static com.alibaba.cloud.ai.graph.action.AsyncEdgeAction.edge_async;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * @author coderpwh
 */
@Configuration
public class WritingAssistantAutoconfiguration {


    @Bean
    public StateGraph writingAssistantStateGraph(ChatModel chatModel) throws GraphStateException {

        ChatClient chatClient = ChatClient.builder(chatModel).defaultAdvisors(new SimpleLoggerAdvisor()).build();

        KeyStrategyFactory keyStrategyFactory = () -> {

            HashMap<String, KeyStrategy> keyStrategyMap = new HashMap<>();

            keyStrategyMap.put("original_text", new ReplaceStrategy());
            keyStrategyMap.put("summary", new ReplaceStrategy());
            keyStrategyMap.put("summary_feedback", new ReplaceStrategy());
            keyStrategyMap.put("reworded", new ReplaceStrategy());
            keyStrategyMap.put("title", new ReplaceStrategy());
            return keyStrategyMap;
        };

        StateGraph stateGraph = new StateGraph(keyStrategyFactory)
                .addNode("summarizer", node_async(new SummarizerNode(chatClient)))
                .addNode("feedback_classifier", node_async(new SummaryFeedbackClassifierNode(chatClient, "summary")))
                .addNode("reworder", node_async(new RewordingNode(chatClient)))
                .addNode("title_generator", node_async(new TitleGeneratorNode(chatClient)))

                .addEdge(START, "summarizer")
                .addEdge("summarizer", "feedback_classifier")
                .addConditionalEdges("feedback_classifier", edge_async(new FeedbackDispatcher()), Map.of("positive", "reworder", "negative", "summarizer"))
                .addEdge("reworder", "title_generator")
                .addEdge("title_generator", END);


        GraphRepresentation representation = stateGraph.getGraph(GraphRepresentation.Type.PLANTUML, "Writing Assistant flow");
        System.out.println("\n=== Writing Assistant UML Flow ===");
        System.out.println(representation.content());
        System.out.println("==================================\n");

        return stateGraph;
    }


    /***
     * 总结类-节点
     */
    static class SummarizerNode implements NodeAction {
        private final ChatClient chatClient;

        public SummarizerNode(ChatClient chatClient) {
            this.chatClient = chatClient;
        }

        @Override
        public Map<String, Object> apply(OverAllState state) throws Exception {
            String text = (String) state.value("original_text").orElse("");
            String prompt = "请对以下中文文本进行简洁明了的摘要：\n\n" + text;

            ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
            String summary = response.getResult().getOutput().getText();

            Map<String, Object> result = new HashMap<>();
            result.put("summary", summary);
            return result;
        }

    }

    /***
     * 摘要反馈分类-节点
     */
    static class SummaryFeedbackClassifierNode implements NodeAction {

        private final ChatClient chatClient;

        private final String inputKey;


        public SummaryFeedbackClassifierNode(ChatClient chatClient, String inputKey) {
            this.chatClient = chatClient;
            this.inputKey = inputKey;
        }


        @Override
        public Map<String, Object> apply(OverAllState state) throws Exception {
            String summary = (String) state.value("summary").orElse("");

            if (!StringUtils.hasText(summary)) {
                throw new IllegalArgumentException("summary is empty in state");
            }

            String prompt = """
                    以下是一个自动生成的中文摘要。请你判断它是否让用户满意。如果满意，请返回 "positive"，否则返回 "negative"：

                    摘要内容：
                    %s
                    """.formatted(summary);

            ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
            String output = response.getResult().getOutput().getText();

            String classification = output.toLowerCase().contains("positive") ? "positive" : "negative";
            Map<String, Object> updated = new HashMap<>();
            updated.put("summary_feedback", classification);

            return updated;
        }

    }


    /***
     * 重写类-节点
     */
    static class RewordingNode implements NodeAction {

        private final ChatClient chatClient;

        public RewordingNode(ChatClient chatClient) {
            this.chatClient = chatClient;
        }


        @Override
        public Map<String, Object> apply(OverAllState state) throws Exception {
            String summary = (String) state.value("summary").orElse("");

            String prompt = "请将以下摘要用更优美、生动的语言改写，同时保持信息不变：\n\n" + summary;

            ChatResponse response = chatClient.prompt(prompt).call().chatResponse();

            String reworded = response.getResult().getOutput().getText();

            Map<String, Object> result = new HashMap<>();
            result.put("reworded", reworded);
            return result;
        }
    }


    /***
     * 标题生成-节点
     */
    static class TitleGeneratorNode implements NodeAction {

        private final ChatClient chatClient;

        public TitleGeneratorNode(ChatClient chatClient) {
            this.chatClient = chatClient;
        }

        @Override
        public Map<String, Object> apply(OverAllState state) throws Exception {

            String reworded = (String) state.value("reworded").orElse("");
            String prompt = "请为以下内容生成一个简洁有吸引力的中文标题：\n\n" + reworded;

            ChatResponse chatResponse = chatClient.prompt(prompt).call().chatResponse();
            String title = chatResponse.getResult().getOutput().getText();

            Map<String, Object> result = new HashMap<>();
            result.put("title", title);
            return result;
        }
    }


    /***
     * 反馈分类-边
     */
    static class FeedbackDispatcher implements EdgeAction {
        @Override
        public String apply(OverAllState state) throws Exception {
            String feedback = (String) state.value("summary_feedback").orElse("");
            if (feedback.contains("positive")) {
                return "positive";
            }
            return "negative";
        }
    }


}
