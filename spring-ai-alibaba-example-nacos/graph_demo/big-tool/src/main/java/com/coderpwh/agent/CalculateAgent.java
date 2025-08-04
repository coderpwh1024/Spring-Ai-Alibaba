package com.coderpwh.agent;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.coderpwh.constants.Constant;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.DefaultToolDefinition;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.ai.tool.support.ToolUtils;
import org.springframework.ai.util.json.schema.JsonSchemaGenerator;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author coderpwh
 */
public class CalculateAgent implements NodeAction {

    private List<Document> documents;

    private ChatClient chatClient;

    private String inputTextKey;

    private String inputText;

    public CalculateAgent(ChatClient chatClient, String inputTextKey) {
        this.chatClient = chatClient;
        this.inputTextKey = inputTextKey;
    }

    private static final String CLASSIFIER_PROMPT_TEMPLATE = """
            ### Job Description
            Please use the tools to complete the task
            """;


    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        if (documents == null) {
            this.documents = (List<Document>) state.value(Constant.TOOL_LIST).orElseThrow();
        }

        List<ToolCallback> toolCallbacks = new ArrayList<>();
        for (Document document : documents) {
            var toolMethod = ReflectionUtils.findMethod(Math.class, document.getMetadata().get(Constant.METHOD_NAME).toString(),
                    (Class<?>[]) document.getMetadata().get(Constant.METHOD_PARAMETER_TYPES));

            DefaultToolDefinition.Builder toolDefinitionBuilder = DefaultToolDefinition.builder()
                    .name(ToolUtils.getToolName(toolMethod))
                    .description(ToolUtils.getToolDescription(toolMethod))
                    .inputSchema(JsonSchemaGenerator.generateForMethodInput(toolMethod));


            MethodToolCallback build = MethodToolCallback.builder()
                    .toolDefinition(toolDefinitionBuilder.build())
                    .toolMethod(toolMethod)
                    .build();

            toolCallbacks.add(build);
        }

        if (StringUtils.hasLength(inputTextKey)) {
            this.inputText = (String) state.value(inputTextKey).orElse(this.inputText);
        }

        ChatResponse chatResponse =  chatClient.prompt().system(CLASSIFIER_PROMPT_TEMPLATE).user(inputText).toolCallbacks(toolCallbacks).call().chatResponse();

        Map<String,Object> updatedState = new HashMap<>();
        updatedState.put(Constant.SOLUTION,chatResponse.getResult().getOutput().getText());
        return updatedState;
    }


}
