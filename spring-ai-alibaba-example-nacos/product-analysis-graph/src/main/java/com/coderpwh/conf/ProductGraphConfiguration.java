package com.coderpwh.conf;

import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.coderpwh.model.Product;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tukaani.xz.rangecoder.RangeEncoderToBuffer;

import java.util.HashMap;
import java.util.Map;

import static com.alibaba.cloud.ai.graph.StateGraph.START;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * @author coderpwh
 */
@Configuration
public class ProductGraphConfiguration {


    @Bean
    public StateGraph productAnalysisGraph(ChatClient.Builder chatClientBuilder) throws GraphStateException {
        ChatClient client = chatClientBuilder.build();

        KeyStrategyFactory keyStrategyFactory = () -> {
            HashMap<String, KeyStrategy> keyStrategyHashMap = new HashMap<>();
            keyStrategyHashMap.put("productDesc", new ReplaceStrategy());
            keyStrategyHashMap.put("slogan", new ReplaceStrategy());
            keyStrategyHashMap.put("productSpec", new ReplaceStrategy());
            keyStrategyHashMap.put("finalProduct", new ReplaceStrategy());
            return keyStrategyHashMap;
        };


        /***
         * slogan  node
         */
        NodeAction marketingCopyNode = state -> {
            String productDesc = (String) state.value("productDesc").orElseThrow();
            String slogan = client.prompt().user("Generate a catchy slogan for a product with the following description:" + productDesc).call().content();
            return Map.of("slogan", slogan);
        };


        /***
         *  product信息 node
         */
        NodeAction specificationExtractionNode = state -> {
            String productDesc = (String) state.value("productDesc").orElseThrow();
            Product product = client.prompt().user("Extract product specifications from the following description: " + productDesc).call().entity(Product.class);
            return Map.of("productSpec", product);
        };


        /***
         * slogan(描述)
         * productDesc(商品信息)
         * 合并 node
         */
        NodeAction mergeNode = state -> {
            String slogan = (String) state.value("slogan").orElseThrow();
            Product productDesc = (Product) state.value("productDesc").orElseThrow();
            Product finalProduct = new Product(slogan, productDesc.material(), productDesc.colors(), productDesc.season());
            return Map.of("finalProduct", finalProduct);
        };


        StateGraph stateGraph = new StateGraph("ProductAnalysisGraph", keyStrategyFactory);
        stateGraph
                .addNode("marketingCopy", node_async(marketingCopyNode))
                .addNode("specificationExtraction", node_async(specificationExtractionNode))
                .addNode("merge", node_async(mergeNode))
                .addEdge(START, "marketingCopy")
                .addEdge(START, "specificationExtraction")
                .addEdge("marketingCopy", "merge")
                .addEdge("specificationExtraction", "merge")
                .addEdge("merge", StateGraph.END);


        GraphRepresentation representation = stateGraph.getGraph(GraphRepresentation.Type.PLANTUML, "Product Analysis Graph");
        System.out.println("\n=== Product Analysis Graph UML Flow ===");
        System.out.println(representation.content());
        System.out.println("======================================\n");

        return stateGraph;
    }


}
