package com.coderpwh.agent;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.coderpwh.constants.Constant;
import com.coderpwh.service.VectorStoreService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author coderpwh
 */
public class ToolAgent implements NodeAction {

    private List<Document> documents;

    private ChatClient chatClient;

    private String inputTextKey;

    private String inputText;


    private VectorStoreService vectorStoreService;

    public ToolAgent(ChatClient chatClient, String inputTextKey, VectorStoreService vectorStoreService) {
        this.chatClient = chatClient;
        this.inputTextKey = inputTextKey;
        this.vectorStoreService = vectorStoreService;
    }

    public ToolAgent(ChatClient chatClient, String inputTextKey, List<Document> documents) {
        this.documents = documents;
        this.chatClient = chatClient;
        this.inputTextKey = inputTextKey;
    }


    private static final String CLASSIFIER_PROMPT_TEMPLATE = """
            ### Job Description
            You are a text keyword extraction engine that can analyze the questions passed in by users and extract the main keywords of this sentence.
            ### Task
            You need to extract one or more keywords from this sentence, without missing the main body of the user description
            ### Constraint
            Multiple keywords returned, separated by spaces
            """;

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {

        if (documents == null) {
            this.documents = (List<Document>) state.value(Constant.TOOL_LIST).orElseThrow();
        }

        if (StringUtils.hasLength(inputTextKey)) {
            this.inputText = (String) state.value(inputTextKey).orElseThrow();
        }

        ChatResponse response = chatClient.prompt().system(CLASSIFIER_PROMPT_TEMPLATE).user(inputText).call().chatResponse();

        List<Document> list = vectorStoreService.search(response.getResult().getOutput().getText(), 3);

        Map<String, Object> updatedState = new HashMap<>();
        updatedState.put(Constant.TOOL_LIST, list);
        if (state.value(inputTextKey).isPresent()) {
            updatedState.put(inputTextKey, response.getResult().getOutput().getText());
        }

        return updatedState;
    }

}
