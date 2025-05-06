package com.coderpwh.autoconfigure.properties;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(McpStreamableClientProperties.CONFIG_PREFIX)
public class McpStreamableClientProperties {

    public static final String CONFIG_PREFIX = "spring.ai.mcp.client.streamable";

    public record StreamableParameters(String url) {

    }

    private final Map<String, StreamableParameters> connections = new HashMap<>();


    public Map<String, StreamableParameters> getConnections() {
        return connections;
    }

}
