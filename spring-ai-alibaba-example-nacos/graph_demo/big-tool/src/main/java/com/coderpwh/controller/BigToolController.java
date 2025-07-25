package com.coderpwh.controller;

import cn.hutool.core.util.IdUtil;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.OverAllStateFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.coderpwh.agent.CalculateAgent;
import com.coderpwh.agent.Tool;
import com.coderpwh.agent.ToolAgent;
import com.coderpwh.constants.Constant;
import com.coderpwh.service.VectorStoreService;
import com.coderpwh.utils.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.ai.document.Document;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author coderpwh
 */
@RestController
@RequestMapping(value = "/bigtool")
public class BigToolController {

    private static final Logger logger = LoggerFactory.getLogger(BigToolController.class);


    private final VectorStoreService vectorStoreService;

    private CompiledGraph compiledGraph;

    private List<Document> documents = new ArrayList<>();


    public BigToolController(VectorStoreService vectorStoreService, ChatModel chatModel) throws GraphStateException {
        this.vectorStoreService = vectorStoreService;
        this.initializeVectorStore();

        ChatClient chatClient = ChatClient.builder(chatModel).defaultAdvisors(new SimpleLoggerAdvisor()).build();

        OverAllStateFactory stateFactory = () -> {
            OverAllState state = new OverAllState();
            state.registerKeyAndStrategy(Constant.INPUT_KEY, new ReplaceStrategy());
            state.registerKeyAndStrategy(Constant.HIT_TOOL, new ReplaceStrategy());
            state.registerKeyAndStrategy(Constant.SOLUTION, new ReplaceStrategy());
            state.registerKeyAndStrategy(Constant.TOOL_LIST, new ReplaceStrategy());
            return state;
        };

        ToolAgent tools = new ToolAgent(chatClient, Constant.INPUT_KEY, vectorStoreService);

        CalculateAgent calculateAgent = new CalculateAgent(chatClient, Constant.INPUT_KEY);

        StateGraph stateGraph = new StateGraph("Consumer Service Workflow Demo", stateFactory)
                .addNode("tools", AsyncNodeAction.node_async(tools))
                .addNode("calculate_agent", AsyncNodeAction.node_async(calculateAgent))
                .addEdge(StateGraph.START, "tools")
                .addEdge("tools", "calculate_agent")
                .addEdge("calculate_agent", StateGraph.END);

        GraphRepresentation graphRepresentation = stateGraph.getGraph(GraphRepresentation.Type.MERMAID, "workflow graph");

        System.out.println("\n\n");
        System.out.println(graphRepresentation.content());
        System.out.println("\n\n");

        this.compiledGraph = stateGraph.compile();
    }

    private void initializeVectorStore() {
        List<Tool> allTools = new ArrayList<>();

        for (Method method : Math.class.getMethods()) {
            if (java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                Tool tool = MethodUtils.convertMethodToTool(method);
                if (tool != null) {
                    allTools.add(tool);
                }
            }
        }

        allTools.forEach(tool -> documents.add(new Document(IdUtil.fastSimpleUUID(), tool.getDescription(),
                Map.of(Constant.METHOD_NAME, tool.getName(), Constant.METHOD_PARAMETER_TYPES, tool.getParameterTypes()))));

        vectorStoreService.addDocuments(documents);
    }


    @GetMapping(value = "/search")
    public String search(String query) throws GraphRunnerException {
        Optional<OverAllState> invoke = compiledGraph.invoke(Map.of(Constant.INPUT_KEY, query, Constant.TOOL_LIST, documents));
        return invoke.get().value("solution").get().toString();
    }


}
