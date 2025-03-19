package com.coderpwh.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.DenseVectorProperty;
import co.elastic.clients.elasticsearch._types.mapping.KeywordProperty;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TextProperty;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import com.alibaba.fastjson.JSON;
import com.coderpwh.service.AzureOpenAiService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.RestClient;
import org.springframework.ai.autoconfigure.vectorstore.elasticsearch.ElasticsearchVectorStoreProperties;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.azure.openai.AzureOpenAiEmbeddingModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStoreOptions;
import org.springframework.stereotype.Service;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.elasticsearch.SimilarityFunction;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStore;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStoreOptions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author coderpwh
 */
@Slf4j
@Service
public class AzureOpenAiServiceImpl implements AzureOpenAiService {


    @Resource
    private   AzureOpenAiChatModel chatModel;


/*    @Resource
    private  VectorStore vectorStore;*/


    @Resource
    private  MessageChatMemoryServiceImpl messageChatMemoryService;


    @Override
    public String chat(String message) {

        List<Advisor> advisorsList = new ArrayList<>();
        advisorsList.add(new PromptChatMemoryAdvisor(new InMemoryChatMemory(), "你是一个智能小助手,回答用户所有的提问，要求风格幽默有趣"));
//        advisorsList.add(new QuestionAnswerAdvisor(vectorStore));
//        log.info("vectorStore:{}", JSON.toJSONString(vectorStore));

        advisorsList.add(new MessageChatMemoryAdvisor(messageChatMemoryService));


        ChatClient chatClient = ChatClient.builder(chatModel).defaultAdvisors(advisorsList).defaultUser(message).build();


        ChatResponse chatResponse = chatClient.prompt().user(message).call().chatResponse();

        log.info("callResponseSpec:{}", JSON.toJSONString(chatResponse));
        String text = chatResponse.getResult().getOutput().getText();

        return text;
    }


    /***
     * 导入数据
     * @return
     */
    @Override
    public String importData() {

        Map<String, Object> map = new ConcurrentHashMap<>();
        map.put("id", 10001);
        map.put("user_id", "coderpwh");
        map.put("user_content", "我喜欢喝可乐");
        map.put("prompt_content", "可乐可以让你非常快乐");
        map.put("create_time", "2025-01-01 00:00:00");

        List<Document> documents = new ArrayList<>();
        documents.add(new Document("我喜欢喝可乐", map));
        List<Document> splitDocuments = new TokenTextSplitter().apply(documents);
        log.info("{} documents split", splitDocuments.size());

        log.info("create embedding and save to vector store");
//        createIndex();
//        log.info("vectorStore:{}", JSON.toJSONString(vectorStore));
//        vectorStore.add(splitDocuments);

        return "success";
    }


/*    public void createIndex() {
        try {
            String indexName = options.getIndexName();
            Integer dimsLength = options.getDimensions();

            if (StringUtils.isBlank(indexName)) {
                throw new IllegalArgumentException("Elastic search index name must be provided");
            }

            boolean exists = elasticsearchClient.indices().exists(idx -> idx.index(indexName)).value();
            if (exists) {
                log.error("当前索引已存在,索引名称为:{}", indexName);
                return;
            }

            String similarityAlgo = options.getSimilarity().name();
            Map<String, Property> properties = new HashMap<>();
            properties.put("embedding", Property.of(property -> property.denseVector(
                    DenseVectorProperty.of(dense -> dense.index(true).dims(dimsLength).similarity(similarityAlgo)))));
            properties.put("id", Property.of(property -> property.keyword(KeywordProperty.of(k -> k))));
            properties.put("user_id", Property.of(property -> property.keyword(KeywordProperty.of(k -> k))));
            properties.put("user_content", Property.of(property -> property.text(TextProperty.of(t -> t))));
            properties.put("prompt_content", Property.of(property -> property.text(TextProperty.of(t -> t))));
            properties.put("create_time", Property.of(property -> property.text(TextProperty.of(d -> d))));

            CreateIndexResponse indexResponse = elasticsearchClient.indices()
                    .create(createIndexBuilder -> createIndexBuilder.index(indexName)
                            .mappings(TypeMapping.of(mappings -> mappings.properties(properties))));
            if (!indexResponse.acknowledged()) {
                log.error("创建索引失败");
            }
            log.info("create elasticsearch index {} successfully", indexName);
        } catch (Exception e) {
            log.error("创建索引异常");
        }

    }*/


}
