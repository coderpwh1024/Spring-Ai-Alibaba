package com.coderpwh;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

//    private String userInput = "武汉的天气如何？";

    private  String  userInput="生成一张小狗的照片";


    @Bean
    public CommandLineRunner predefinedQuestions(ChatClient.Builder chatClientBuilder, ToolCallbackProvider tools, ConfigurableApplicationContext context) {

        return args -> {

            var chatClient = chatClientBuilder.defaultTools(tools).build();

            System.out.println("问题:" + userInput);
            System.out.println("助理回答:"+ chatClient.prompt(userInput).call().content());

            context.close();
        };

    }


}
