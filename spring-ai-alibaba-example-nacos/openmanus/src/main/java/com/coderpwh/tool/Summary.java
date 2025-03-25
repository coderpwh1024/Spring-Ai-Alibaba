package com.coderpwh.tool;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.coderpwh.agent.AgentState;
import com.coderpwh.agent.BaseAgent;
import com.coderpwh.tool.support.ToolExecuteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.tool.metadata.ToolMetadata;


/**
 * @author coderpwh
 */
public class Summary  implements BiFunction<String, ToolContext, ToolExecuteResult> {

    private static final Logger log = LoggerFactory.getLogger(Summary.class);

    private static String PARAMETERS = """
			{
			  "type" : "object",
			  "properties" : {
			    "summary" : {
			      "type" : "string",
			      "description" : "The output of current step, better make a summary."
			    }
			  },
			  "required" : [ "summary" ]
			}
			""";

    private static final String name = "summary";

    private static final String description = "Record the summary of current step.";

    public static OpenAiApi.FunctionTool getToolDefinition() {
        OpenAiApi.FunctionTool.Function function = new OpenAiApi.FunctionTool.Function(description, name, PARAMETERS);
        OpenAiApi.FunctionTool functionTool = new OpenAiApi.FunctionTool(function);
        return functionTool;
    }

    public static FunctionToolCallback getFunctionToolCallback(BaseAgent agent, ChatMemory chatMemory,
                                                               String conversationId) {
        return FunctionToolCallback.builder(name, new Summary(agent, chatMemory, conversationId))
                .description(description)
                .inputSchema(PARAMETERS)
                .inputType(String.class)
                .toolMetadata(ToolMetadata.builder().returnDirect(true).build())
                .build();
    }

    private BaseAgent agent;

    private ChatMemory chatMemory;

    private String conversationId;

    public Summary(BaseAgent agent, ChatMemory chatMemory, String conversationId) {
        this.agent = agent;
        this.chatMemory = chatMemory;
        this.conversationId = conversationId;
    }

    public ToolExecuteResult run(String toolInput) {
        log.info("Summary toolInput:" + toolInput);
        agent.setState(AgentState.FINISHED);
        return new ToolExecuteResult(toolInput);
    }

    @Override
    public ToolExecuteResult apply(String s, ToolContext toolContext) {
        // chatMemory.add(conversationId, toolContext.getToolCallHistory());
        return run(s);
    }
}
