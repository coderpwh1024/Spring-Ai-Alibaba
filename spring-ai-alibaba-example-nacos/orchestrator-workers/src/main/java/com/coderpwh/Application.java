package com.coderpwh;

import com.coderpwh.work.OrchestratorWorkers;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author coderpwh
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ChatClient.Builder chatClientBuilder){
        var chatClient = chatClientBuilder.build();
        return args -> {
             new OrchestratorWorkers(chatClient).process("帮助依依找个富婆");
        };
    }


}
