package com.coderpwh;

import com.coderpwh.service.DogService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DogApplication {

    public static void main(String[] args) {
        SpringApplication.run(DogApplication.class, args);
    }


    @Bean
    public ToolCallbackProvider weatherTools(DogService dogService) {
        return MethodToolCallbackProvider.builder().toolObjects(dogService).build();
    }

}
