package com.coderpwh;

import io.modelcontextprotocol.client.transport.ServerParameters;
import org.springframework.web.reactive.function.client.WebClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import java.io.File;

public class ClientStdio {

    public static void main(String[] args) {

        System.out.println(new File(".").getAbsolutePath());

        var stdioParams = ServerParameters.builder("java")
                .args("-Dspring.ai.mcp.server.stdio=true",
                        "-Dspring.main.web-application-type=none",
                        "-Dlogging.pattern.console=",
                        "-jar",
                        "/Users/coderpwh/workSpace/Spring-Ai-Alibaba/spring-ai-alibaba-example-nacos/mcp/start/server/starter-stdio-server/target/starter-stdio-server-1.0-SNAPSHOT.jar")
                .build();

        var transport = new StdioClientTransport(stdioParams);

        new SampleClient(transport).run();

    }

}
