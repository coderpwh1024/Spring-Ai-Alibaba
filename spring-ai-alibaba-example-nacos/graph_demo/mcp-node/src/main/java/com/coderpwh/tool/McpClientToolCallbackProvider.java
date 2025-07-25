package com.coderpwh.tool;

import com.coderpwh.config.McpNodeProperties;
import org.apache.commons.compress.utils.Lists;
import org.glassfish.jersey.internal.guava.Sets;
import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.ai.mcp.client.autoconfigure.properties.McpClientCommonProperties;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * @author coderpwh
 */
@Service
public class McpClientToolCallbackProvider {


    private final ToolCallbackProvider toolCallbackProvider;


    private final McpClientCommonProperties mcpClientCommonProperties;

    private final McpNodeProperties mcpNodeProperties;


    public McpClientToolCallbackProvider(ToolCallbackProvider toolCallbackProvider, McpClientCommonProperties mcpClientCommonProperties, McpNodeProperties mcpNodeProperties) {
        this.toolCallbackProvider = toolCallbackProvider;
        this.mcpClientCommonProperties = mcpClientCommonProperties;
        this.mcpNodeProperties = mcpNodeProperties;
    }

    public Set<ToolCallback> findToolCallbacks(String nodeName) {

        Set<ToolCallback> defineCallback = Sets.newHashSet();
        Set<String> mcpClients = mcpNodeProperties.getNode2servers().get(nodeName);
        if (mcpClients == null || mcpClients.isEmpty()) {
            return defineCallback;
        }

        List<String> exceptMcpClientNames = Lists.newArrayList();
        for (String mcpClient : mcpClients) {
            String name = mcpClientCommonProperties.getName();
            String prefixedMcpClientName = McpToolUtils.prefixedToolName(name, mcpClient);
            exceptMcpClientNames.add(prefixedMcpClientName);
        }

        ToolCallback[] toolCallbacks = toolCallbackProvider.getToolCallbacks();

        for (ToolCallback toolCallback : toolCallbacks) {
            ToolDefinition toolDefinition =toolCallback.getToolDefinition();
            String name =toolDefinition.name();
            for(String exceptMcpClientName:exceptMcpClientNames){
                if(name.startsWith(exceptMcpClientName)){
                    defineCallback.add(toolCallback);
                }
            }
        }
        return defineCallback;
    }


}
