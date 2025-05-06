package com.coderpwh.autoconfigure;


import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.coderpwh.autoconfigure.properties.McpStreamableClientProperties;
import com.coderpwh.io.modelcontextprotocol.client.transport.StreamableHttpClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.mcp.client.autoconfigure.NamedClientMcpTransport;
import org.springframework.ai.mcp.client.autoconfigure.properties.McpClientCommonProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
@AutoConfiguration
@ConditionalOnClass({ McpSchema.class, McpSyncClient.class })
@EnableConfigurationProperties({ McpStreamableClientProperties.class, McpClientCommonProperties.class })
@ConditionalOnProperty(prefix = McpClientCommonProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true",
        matchIfMissing = true)

public class StreamableHttpClientTransportAutoConfiguration {


    public StreamableHttpClientTransportAutoConfiguration() {

    }


    @Bean
    public List<NamedClientMcpTransport> mcpHttpClientTransports(McpStreamableClientProperties streamableProperties,
                                                                 ObjectProvider<ObjectMapper> objectMapperProvider) {

        ObjectMapper objectMapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);
        List<NamedClientMcpTransport> sseTransports = new ArrayList<>();
        for (Map.Entry<String, McpStreamableClientProperties.StreamableParameters> serverParameters : streamableProperties.getConnections().entrySet()) {
            var transport = StreamableHttpClientTransport.builder(serverParameters.getValue().url()).withObjectMapper(objectMapper).build();
            sseTransports.add(new NamedClientMcpTransport(serverParameters.getKey(), transport));
        }

        return sseTransports;
    }


}
