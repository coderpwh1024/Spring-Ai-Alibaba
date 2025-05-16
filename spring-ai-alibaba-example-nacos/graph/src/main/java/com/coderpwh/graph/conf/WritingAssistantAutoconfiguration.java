package com.coderpwh.graph.conf;

import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.GraphStateException;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncEdgeAction;
import com.alibaba.cloud.ai.graph.state.AgentStateFactory;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.coderpwh.graph.dispatcher.FeedbackDispatcher;
import com.coderpwh.graph.node.RewordingNode;
import com.coderpwh.graph.node.SummarizerNode;
import com.coderpwh.graph.node.SummaryFeedbackClassifierNode;
import com.coderpwh.graph.node.TitleGeneratorNode;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static com.alibaba.cloud.ai.graph.StateGraph.START;
import static com.alibaba.cloud.ai.graph.action.AsyncEdgeAction.edge_async;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * @author coderpwh
 */
@Configuration
public class WritingAssistantAutoconfiguration {


    @Bean
    public StateGraph writingAssistantGraph(ChatModel chatModel) throws GraphStateException {


        ChatClient chatClient = ChatClient.builder(chatModel).defaultAdvisors(new SimpleLoggerAdvisor()).build();

        AgentStateFactory<OverAllState> stateFactory = (inputs)->{
            OverAllState state = new OverAllState();
            state.registerKeyAndStrategy("original_text", new ReplaceStrategy());
            state.registerKeyAndStrategy("summary", new ReplaceStrategy());
            state.registerKeyAndStrategy("summary_feedback", new ReplaceStrategy());
            state.registerKeyAndStrategy("reworded", new ReplaceStrategy());
            state.registerKeyAndStrategy("title", new ReplaceStrategy());
            state.input(inputs);
            return  state;
        };

        StateGraph graph = new StateGraph("Writing Assistant with Feedback Loop",stateFactory)
                .addNode("summarizer", node_async(new SummarizerNode(chatClient)))
                .addNode("feedback_classifier",node_async(new SummaryFeedbackClassifierNode(chatClient,"summary")))
                .addNode("reworder",node_async(new RewordingNode(chatClient)))
                .addNode("title_generator",node_async(new TitleGeneratorNode(chatClient)))

                .addEdge(START,"summarizer")
                .addEdge("summarizer","feedback_classifier")
                .addConditionalEdges("feedback_classifier", edge_async(new FeedbackDispatcher()), Map.of("positive", "reworder", "negative", "summarizer"))
                .addEdge("reworder","title_generator")
                .addEdge("title_generator", StateGraph.END);

        GraphRepresentation representation = graph.getGraph(GraphRepresentation.Type.PLANTUML,
                "writing assistant flow");
        System.out.println("\n=== Writing Assistant UML Flow ===");
        System.out.println(representation.content());
        System.out.println("==================================\n");
        return  graph;
    }


}
