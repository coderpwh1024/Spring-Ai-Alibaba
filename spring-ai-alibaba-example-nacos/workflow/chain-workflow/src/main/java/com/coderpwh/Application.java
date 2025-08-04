package com.coderpwh;

import com.coderpwh.work.ChainWorkflow;
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


    String report = """
            慧慧想开一家咖啡馆，他希望这个咖啡馆既能吸引年轻人，又能兼顾上班族的需求。请帮他设计一个可行的咖啡馆运营方案。
            """;

    @Bean
    public CommandLineRunner commandLineRunner(ChatClient.Builder chatClientBuilder) {
        return args -> {
            new ChainWorkflow(chatClientBuilder.build()).chain(report);
        };
    }


}
