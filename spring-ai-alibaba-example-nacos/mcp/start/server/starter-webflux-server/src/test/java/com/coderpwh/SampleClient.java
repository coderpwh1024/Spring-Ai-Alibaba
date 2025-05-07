package com.coderpwh;

import java.util.Map;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.spec.ClientMcpTransport;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;

public class SampleClient {

    private final ClientMcpTransport clientMcpTransport;

    public SampleClient(ClientMcpTransport clientMcpTransport) {
        this.clientMcpTransport = clientMcpTransport;
    }


    public void run() {

        var client = McpClient.sync(this.clientMcpTransport).build();

        client.initialize();

        client.ping();

        ListToolsResult toolsResult = client.listTools();

        System.out.println("可用工具="+toolsResult);

        CallToolResult weatherForecastResult = client.callTool(new CallToolRequest("getWeatherForecastByLocation", Map.of("latitude", 39.9042, "longitude", 116.4074)));
        System.out.println("天气预报:"+weatherForecastResult);

        // 获取北京的空气质量信息
        CallToolResult airQualityResult = client.callTool(new CallToolRequest("getAirQuality",
                Map.of("latitude", "39.9042", "longitude", "116.4074")));
        System.out.println("北京空气质量: " + airQualityResult);

        client.closeGracefully();





    }


}
