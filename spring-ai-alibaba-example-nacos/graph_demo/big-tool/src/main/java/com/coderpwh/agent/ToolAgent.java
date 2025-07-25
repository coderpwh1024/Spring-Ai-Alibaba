package com.coderpwh.agent;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.coderpwh.service.VectorStoreService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.Map;

/**
 * @author coderpwh
 */
public class ToolAgent implements NodeAction {


    private List<Document> documents;

    private ChatClient chatClient;

    private String inputTextKey;

    private  String inputText;


    private VectorStoreService vectorStoreService;


    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        return null;
    }

}
