package com.coderpwh.conf;

import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.coderpwh.model.Product;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author coderpwh
 */
@Configuration
public class ProductGraphConfiguration {


    @Bean
    public StateGraph productGraph(ChatClient.Builder chatClientBuilder) {
        ChatClient client = chatClientBuilder.build();

        KeyStrategyFactory keyStrategyFactory = () -> {
            HashMap<String, KeyStrategy> keyStrategyHashMap = new HashMap<>();
            keyStrategyHashMap.put("productDesc", new ReplaceStrategy());
            keyStrategyHashMap.put("slogan", new ReplaceStrategy());
            keyStrategyHashMap.put("productSpec", new ReplaceStrategy());
            keyStrategyHashMap.put("finalProduct", new ReplaceStrategy());
            return keyStrategyHashMap;
        };


        NodeAction marketingCopyNode = state -> {
            String productDesc = (String) state.value("productDesc").orElseThrow();
            String slogan = client.prompt().user("Generate a catchy slogan for a product with the following description: " + productDesc).call().content();
            return Map.of("slogan", slogan);
        };


        NodeAction specificationExtractionNode = state -> {
            String productDesc = (String) state.value("productDesc").orElseThrow();
            Product product = client.prompt().user("Extract product specifications from the following description: " + productDesc).call().entity(Product.class);
            return Map.of("productSpec", product);
        };







         return  null;
    }


}
