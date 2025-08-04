package com.coderpwh.graph.conf;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.OverAllStateFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.node.AnswerNode;
import com.alibaba.cloud.ai.graph.node.DocumentExtractorNode;
import com.alibaba.cloud.ai.graph.node.HttpNode;
import com.alibaba.cloud.ai.graph.node.HumanNode;
import com.alibaba.cloud.ai.graph.node.KnowledgeRetrievalNode;
import com.alibaba.cloud.ai.graph.node.LlmNode;
import com.alibaba.cloud.ai.graph.node.ParameterParsingNode;
import com.alibaba.cloud.ai.graph.node.QuestionClassifierNode;
import com.alibaba.cloud.ai.graph.node.ToolNode;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.resolution.ToolCallbackResolver;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.StateGraph.START;

/**
 * @author coderpwh
 */
@Component
public class ComplexSupportGraphBuilder {


    @Bean
    public CompiledGraph buildGraph(ChatModel chatModel, VectorStore vectorStore, ToolCallbackResolver toolCallbackResolver) throws GraphStateException {

        ChatClient chatClient = ChatClient.builder(chatModel).defaultAdvisors(new SimpleLoggerAdvisor()).build();

        OverAllStateFactory overAllStateFactory = () -> {
            OverAllState state = new OverAllState();
            for (String key : List.of("input", "attachments", "docs", "parameterParsing_output", "classifier_output",
                    "retrieved_docs", "filtered_docs", "http_response", "llm_response", "tool_result", "human_feedback",
                    "answer")) {
                state.registerKeyAndStrategy(key, (o1, o2) -> o2);
            }
            return state;
        };

        StateGraph graph = new StateGraph(overAllStateFactory);


        DocumentExtractorNode extractNode = DocumentExtractorNode.builder().fileList(List.of("data/manual.txt"))
                .paramsKey("attachments")
                .outputKey("docs")
                .build();
        graph.addNode("extractDocs", AsyncNodeAction.node_async(extractNode));

        ParameterParsingNode paramNode = ParameterParsingNode.builder()
                .chatClient(chatClient)
                .inputTextKey("input")
                .parameters(List.of(Map.of("name", "ticketId", "type", "string", "description", "工单编号"),
                        Map.of("name", "priority", "type", "string", "description", "优先级")))
                .build();
        graph.addNode("parseParams", AsyncNodeAction.node_async(paramNode));


        QuestionClassifierNode qcNode = QuestionClassifierNode.builder()
                .chatClient(chatClient)
                .inputTextKey("input")
                .categories(List.of("售后", "技术支持", "投诉", "咨询"))
                .classificationInstructions(List.of("请仅返回最合适的类别名称String类型，例如：售后、运输、产品质量、其他；不要多余的标记或格式。 正确返回结果： 售后 "))
                .build();
        graph.addNode("classify", AsyncNodeAction.node_async(qcNode));


        KnowledgeRetrievalNode krNode = KnowledgeRetrievalNode.builder()
                .userPromptKey("classifier_output")
                .vectorStore(vectorStore)
                .topK(5)
                .similarityThreshold(0.5)
                .enableRanker(false)
                .build();
        graph.addNode("retrieveDocs", AsyncNodeAction.node_async(krNode));

        HttpNode httpNode = HttpNode.builder()
                .webClient(WebClient.builder().build())
                .method(HttpMethod.GET)
                .url("http://localhost:8080/api/graph/mock/http?" + "ticketId=12345" + "&category=售后")
                .outputKey("http_response")
                .build();
        graph.addNode("syncTicket", AsyncNodeAction.node_async(httpNode));


        LlmNode llmNode = LlmNode.builder()
                .chatClient(chatClient)
                .systemPromptTemplate("你是客服助手，请基于以下信息撰写回复：")
                .userPromptTemplateKey("http_response")
                .messagesKey("user_prompt")
                .outputKey("llm_response")
                .build();
        graph.addNode("invokeLLM", AsyncNodeAction.node_async(llmNode));

        ToolNode toolNode = ToolNode.builder()
                .llmResponseKey("llm_response")
                .outputKey("tool_result")
                .toolCallbackResolver(toolCallbackResolver)
                .toolNames(List.of("sendEmail", "updateCRM"))
                .build();
        graph.addNode("invokeTool", AsyncNodeAction.node_async(toolNode));


        HumanNode humanNode = new HumanNode("conditioned",
                st -> st.value("tool_result").map(r -> r.toString().contains("ERROR")).orElse(false),
                st -> Map.of("answer", st.value("tool_result").orElse("").toString()));
        graph.addNode("humanReview", AsyncNodeAction.node_async(humanNode));


        AnswerNode ansNode = AnswerNode.builder().answer("{{answer}}").build();
        graph.addNode("finalAnswer", AsyncNodeAction.node_async(ansNode));

        graph.addEdge(START, "extractDocs")
                .addEdge("extractDocs", "parseParams")
                .addEdge("parseParams", "classify")
                .addEdge("classify", "retrieveDocs")
                .addEdge("retrieveDocs", "syncTicket")
                // .addEdge("filterDocs", "syncTicket")
                .addEdge("syncTicket", "invokeLLM")
                .addEdge("invokeLLM", "invokeTool")
                .addEdge("invokeTool", "humanReview")
                .addEdge("humanReview", "finalAnswer")
                .addEdge("finalAnswer", END);
        return graph.compile();
    }


}
