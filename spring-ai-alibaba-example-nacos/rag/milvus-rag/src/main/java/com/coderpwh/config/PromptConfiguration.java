package com.coderpwh.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author coderpwh
 */
@Configuration
public class PromptConfiguration {


    @Bean
    public ChatClient chatClient(ChatClient.Builder  builder){
        return builder.defaultSystem("你将作为一名 Spring-AI-Alibaba 的专家，对于用户的使用需求作出解答")
                .build();
    }


}
