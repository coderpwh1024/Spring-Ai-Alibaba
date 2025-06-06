package com.coderpwh;

import com.coderpwh.work.EvaluatorOptimizer;
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
    public CommandLineRunner commandLineRunner(ChatClient.Builder chatClientBuilder) {
        var chatClient = chatClientBuilder.build();
        return args -> {

            EvaluatorOptimizer.RefinedResponse refinedResponse = new EvaluatorOptimizer(chatClient)
                    .loop("""
                            <user input>
                            Implement a Stack in Java with:
                            1. push(x)
                            2. pop()
                            3. getMin()
                            All operations should be O(1).
                            All inner fields should be private and when used should be prefixed with 'this.'.
                            </user input>
                            """);

            System.out.println("FINAL OUTPUT:\n:" + refinedResponse);

        };

    }


}
