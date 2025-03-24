package com.coderpwh.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import com.alibaba.fastjson.JSON;
import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatRequestSystemMessage;
import com.azure.core.credential.AzureKeyCredential;
import com.coderpwh.entity.request.ElasticSearchRequestDTO;
import com.coderpwh.entity.request.VectorStoreRequestDTO;
import com.coderpwh.service.AzureElasticSearchService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AzureElasticSearchServiceImpl implements AzureElasticSearchService {



    @Resource
    private   ElasticsearchClient elasticsearchClient;

    @Override
    public String chat(String message) {

        String endpoint = "";
        String key = "";
        String deploymentId = "";



        List<ElasticSearchRequestDTO>  list =  getMatchQueryByUserId(message,"coderpwh");
        log.info("list:{}",JSON.toJSONString(list));

        String systemPrompt = """
                你是一个智能助手，幽默有趣回答用户的问题
                """;

        if (!CollectionUtils.isEmpty(list)) {
            StringBuffer buffer = new StringBuffer();
            for (ElasticSearchRequestDTO elasticSearchRequestDTO : list) {
                buffer.append(elasticSearchRequestDTO.getContent());
            }
            systemPrompt += buffer.toString();
        }

        OpenAIClient client = new OpenAIClientBuilder().endpoint(endpoint).credential(new AzureKeyCredential(key)).buildClient();
        List<ChatRequestMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatRequestSystemMessage(systemPrompt));

        ChatCompletions chatCompletionsAnswer = client.getChatCompletions(deploymentId, new ChatCompletionsOptions(chatMessages));
        log.info("大模型结果为:{}", JSON.toJSONString(chatCompletionsAnswer));
        String result = chatCompletionsAnswer.getChoices().get(0).getMessage().getContent();

        return result;
    }

    public List<ElasticSearchRequestDTO>  getMatchQueryByUserId(String text, String userId) {
        List<ElasticSearchRequestDTO> list = new ArrayList<>();
        try {
            log.info("match-query查询,text:{},userId:{}", text, userId);
            SearchResponse<ElasticSearchRequestDTO> response = elasticsearchClient.search(s ->
                            s.index("chat_memory_index")
                                    .source(s1->s1.filter(v->v.excludes("embedding")))
                                    .query(r ->
                                            r.match(m ->
                                                    m.field("metadata.user_content").query(text))
                                    ).postFilter(p -> p.bool(b -> b.filter(f -> f.term(t -> t.field("metadata.user_id").value(userId)))
                                            )).size(10)
                    , ElasticSearchRequestDTO.class);
            HitsMetadata<ElasticSearchRequestDTO> hits = response.hits();
            long totalValue = hits.total().value();
            log.info("match-query查询总数量:{}", totalValue);
            hits.hits().forEach(h -> {
                list.add(h.source());
            });
        } catch (Exception e) {
            log.error("match query 查询异常,异常信息为:{}", e.getMessage());
        }
        return list;
    }


}
