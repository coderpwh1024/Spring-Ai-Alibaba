package com.coderpwh.config;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Resource
    private ToolCallbackProvider toolCallbackProvider;


    @Resource
    private ChatModel chatModel;


    @Bean
    public CommandLineRunner predefinedQuestions(ConfigurableApplicationContext context) {

        return args -> {
            var chatClient = ChatClient.builder(chatModel).build();
            String userInput = "帮我将这个网页内容进行抓取 https://www.shuaijiao.cn/news/view/68320.html";
            System.out.println("\n>>> QUESTION: " + userInput);
            System.out.println("\n>>> ASSISTANT: " + chatClient.prompt().user(userInput).call().content());
            context.close();
        };
    }


}
