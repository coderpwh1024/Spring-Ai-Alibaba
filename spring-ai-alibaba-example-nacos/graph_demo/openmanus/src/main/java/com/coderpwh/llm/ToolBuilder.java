package com.coderpwh.llm;


import java.util.List;

import com.coderpwh.agent.BaseAgent;


import com.coderpwh.tool.BrowserUseTool;
import com.coderpwh.tool.FileSaver;
import com.coderpwh.tool.GoogleSearch;
import com.coderpwh.tool.PlanningTool;
import com.coderpwh.tool.PythonExecute;
import com.coderpwh.tool.Summary;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.tool.ToolCallback;

/**
 * @author coderpwh
 */
public class ToolBuilder {

    public static List<FunctionCallback> getManusAgentToolCalls(BaseAgent agent, ChatMemory memory,
                                                                String conversationId) {
        return List.of(GoogleSearch.getFunctionToolCallback(), BrowserUseTool.getFunctionToolCallback(),
                FileSaver.getFunctionToolCallback(), PythonExecute.getFunctionToolCallback(),
                Summary.getFunctionToolCallback(agent, memory, conversationId));
    }

    public static List<ToolCallback> getManusAgentToolCalls() {
        return List.of(GoogleSearch.getFunctionToolCallback(), BrowserUseTool.getFunctionToolCallback(),
                FileSaver.getFunctionToolCallback(), PythonExecute.getFunctionToolCallback());
    }

    public static List<ToolCallback> getPlanningAgentToolCallbacks() {
        return List.of(PlanningTool.getFunctionToolCallback());
    }

}
