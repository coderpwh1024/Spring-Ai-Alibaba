package com.coderpwh.mcp.streamble;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(exclude = {
        org.springframework.ai.mcp.client.autoconfigure.SseHttpClientTransportAutoConfiguration.class,
})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


    private String userInput = "阿里巴巴西溪园区";

    public CommandLineRunner predefinedQuestions(ChatClient.Builder chatClientBuilder, ToolCallbackProvider tools, ConfigurableApplicationContext context) {

        return args -> {
            var chatClient = chatClientBuilder.defaultTools(tools).build();
            System.out.println("问题:" + userInput);
            System.out.println("助理回答:" + chatClient.prompt().call().content());

            System.out.println("问题:" + "黄金价格走势");
            System.out.println("助理回答:" + chatClient.prompt("黄金价格走势").call().content());

        };
    }


}
