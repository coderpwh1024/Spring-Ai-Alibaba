package com.coderpwh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author coderpwh
 */
@EnableDiscoveryClient
@SpringBootApplication
public class PromptExampleApplication {


    public static void main(String[] args) {

        SpringApplication.run(PromptExampleApplication.class, args);
    }


}
