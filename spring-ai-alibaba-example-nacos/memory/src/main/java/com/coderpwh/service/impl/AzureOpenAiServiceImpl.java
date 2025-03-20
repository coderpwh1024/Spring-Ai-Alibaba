package com.coderpwh.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.DenseVectorProperty;
import co.elastic.clients.elasticsearch._types.mapping.KeywordProperty;
import co.elastic.clients.elasticsearch._types.mapping.ObjectProperty;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TextProperty;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
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
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.VectorStoreChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStoreOptions;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author coderpwh
 */
@Slf4j
@Service
public class AzureOpenAiServiceImpl implements AzureOpenAiService {

    private VectorStore vectorStore;
    private ChatClient chatClient;

    private final ElasticsearchClient elasticsearchClient;

    private final ElasticsearchVectorStoreProperties options;

    public AzureOpenAiServiceImpl(ElasticsearchClient elasticsearchClient, ElasticsearchVectorStoreProperties options, VectorStore vectorStore, ChatClient.Builder clientBuilder) {
        this.elasticsearchClient = elasticsearchClient;
        this.options = options;
        this.vectorStore = vectorStore;
        this.chatClient = clientBuilder.build();
    }


    @Resource
    private MessageChatMemoryServiceImpl messageChatMemoryService;


    @Override
    public String chat(String message) {

        String userId = "coderpwh";


        FilterExpressionBuilder b = new FilterExpressionBuilder();
        Filter.Expression expression = b.eq("user_id", userId).build();

        SearchRequest searchRequest = SearchRequest.builder()
                .query(message)
//                .topK(6)
//                .similarityThreshold(0.8d)
                .filterExpression(expression)
                .build();

        List<Document> list = vectorStore.similaritySearch(searchRequest);
        log.info("list:{}", JSON.toJSONString(list));


        List<Advisor> advisorsList = new ArrayList<>();
        advisorsList.add(new PromptChatMemoryAdvisor(new InMemoryChatMemory(), userId, 10, "你是一个智能小助手,回答用户所有的提问，要求风格幽默有趣", 13));
        advisorsList.add(new MessageChatMemoryAdvisor(messageChatMemoryService, userId, 10, 10));
        advisorsList.add(new QuestionAnswerAdvisor(vectorStore, searchRequest, "根据上下文内容进行回答", true, 12));
        advisorsList.add(new SimpleLoggerAdvisor());
        log.info("vectorStore:{}", JSON.toJSONString(vectorStore));


        ChatResponse chatResponse = this.chatClient.prompt().advisors(advisorsList).user(message).call().chatResponse();

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

        String content = "我非常喜欢看书";

        Map<String, Object> map = new ConcurrentHashMap<>();
        map.put("id", 10003);
        map.put("user_id", "abc");
        map.put("user_content", content);
        map.put("prompt_content", "书是人类进步的阶梯");
        map.put("create_time", "2025-03-20 16:20:00");

        List<Document> documents = new ArrayList<>();
        documents.add(new Document(content, map));

        List<Document> splitDocuments = new TokenTextSplitter().apply(documents);
        log.info("{} documents split", splitDocuments.size());

        log.info("create embedding and save to vector store");
//        createIndex();
        log.info("vectorStore:{}", JSON.toJSONString(vectorStore));
        vectorStore.add(splitDocuments);

        return "success";
    }


    public void createIndex() {
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

            IndexSettings indexSettings = IndexSettings
                    .of(settings -> settings.numberOfShards(String.valueOf(1)).numberOfReplicas(String.valueOf(1)));


            Map<String, Property> properties = new HashMap<>();
            properties.put("embedding", Property.of(property -> property.denseVector(
                    DenseVectorProperty.of(dense -> dense.index(true).similarity(similarityAlgo)))));
            properties.put("content", Property.of(property -> property.text(TextProperty.of(t -> t))));

            Map<String, Property> metadata = new HashMap<>();
            metadata.put("id", Property.of(property -> property.keyword(KeywordProperty.of(k -> k))));
            metadata.put("user_id", Property.of(property -> property.keyword(KeywordProperty.of(k -> k))));
            metadata.put("user_content", Property.of(property -> property.text(TextProperty.of(t -> t))));
            metadata.put("prompt_content", Property.of(property -> property.text(TextProperty.of(t -> t))));
            metadata.put("create_time", Property.of(property -> property.text(TextProperty.of(d -> d))));

            properties.put("metadata",
                    Property.of(property -> property.object(ObjectProperty.of(op -> op.properties(metadata)))));

            CreateIndexResponse indexResponse = elasticsearchClient.indices()
                    .create(createIndexBuilder -> createIndexBuilder.index(indexName)
                            .settings(indexSettings)
                            .mappings(TypeMapping.of(mappings -> mappings.properties(properties))));
            if (!indexResponse.acknowledged()) {
                log.error("创建索引失败");
            }
            log.info("create elasticsearch index {} successfully", indexName);
        } catch (Exception e) {
            log.error("创建索引异常");
        }

    }


}
