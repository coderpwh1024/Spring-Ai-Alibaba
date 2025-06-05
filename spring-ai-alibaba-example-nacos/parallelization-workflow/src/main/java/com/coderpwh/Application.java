package com.coderpwh;

import com.coderpwh.work.ParallelizationlWorkflow;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

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

        String prompt = """
                Analyze how market changes will impact this stakeholder group.
                Provide specific impacts and recommended actions.
                Format with clear sections and priorities.
                """;

        String strOne = """
                 Customers:
                 - Price sensitive
                 - Want better tech
                  - Environmental concerns
                """;

        String strTwo = """
                Employees:
                - Job security worries
                - Need new skills
                - Want clear direction
                """;

        String strThree = """
                Suppliers:
                - Capacity constraints
                - Price pressures
                - Tech transitions
                 """;

        List<String> list = new ArrayList<>();
        list.add(strOne);
        list.add(strTwo);
        list.add(strThree);

        return args -> {
            List<String> parallelResponse = new ParallelizationlWorkflow(chatClientBuilder.build())
                    .parallel(prompt, list, 4);
            System.out.println(parallelResponse);
        };

    }


}
