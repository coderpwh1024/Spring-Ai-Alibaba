package com.coderpwh.node;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.async.AsyncGenerator;
import com.alibaba.cloud.ai.graph.streaming.StreamingChatGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author coderpwh
 */
public class ExpanderNode {

    private static final Logger logger = LoggerFactory.getLogger(ExpanderNode.class);

    private static final PromptTemplate DEFAULT_PROMPT_TEMPLATE = new PromptTemplate("You are an expert at information retrieval and search optimization.\nYour task is to generate {number} different versions of the given query.\n\nEach variant must cover different perspectives or aspects of the topic,\nwhile maintaining the core intent of the original query. The goal is to\nexpand the search space and improve the chances of finding relevant information.\n\nDo not explain your choices or add any other text.\nProvide the query variants separated by newlines.\n\nOriginal query: {query}\n\nQuery variants:\n");

    private final ChatClient chatClient;

    private final Integer NUMBER = 3;


    public ExpanderNode(ChatClient chatClient) {
        this.chatClient = chatClient;
    }


    public Map<String, Object> apply(OverAllState state) {
        logger.info("expander node is running");

        String expandStatus = state.value("expand_status", "");
        logger.info("Current expand_status: {}", expandStatus);

        if(!"assigned".equals(expandStatus)){
            return  Map.of();
        }

        String query = state.value("query", "");
        Integer expandNumber = state.value("expand_number", this.NUMBER);

        logger.info("Calling LLM for expansion, setting status to processing");

        Flux<ChatResponse> chatResponseFlux = this.chatClient.prompt()
                .user((user)->user.text(DEFAULT_PROMPT_TEMPLATE.getTemplate())
                        .param("number",expandNumber)
                        .param("query",query))
                .stream().chatResponse();

        AsyncGenerator<? extends NodeOutput> generator = StreamingChatGenerator.builder()
                .startingNode("expander_llm_stream")
                .startingState(state)
                .mapResult(reponse->{
                    String text = reponse.getResult().getOutput().getText();
                    List<String> queryVariants = Arrays.asList(text.split("\n"));
                    return Map.of("expander_content", queryVariants, "expand_status", "completed");
                }).build(chatResponseFlux);

        return  Map.of("expander_content",generator,"expand_status","processing");
    }


}
