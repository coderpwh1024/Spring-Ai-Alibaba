package com.coderpwh.service.impl;

import com.alibaba.fastjson.JSON;
import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsJsonResponseFormat;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatRequestSystemMessage;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.core.credential.AzureKeyCredential;
import com.coderpwh.common.config.AzureConfig;
import com.coderpwh.service.AzureOpenAiService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author coderpwh
 */
@Slf4j
@Service
public class AzureOpenAiServiceImpl implements AzureOpenAiService {


    @Resource
    private AzureConfig azureConfig;


    @Override
    public String chat(String message) {
//        log.info("azureConfig:{}",JSON.toJSONString(azureConfig));

        OpenAIClient client = new OpenAIClientBuilder().endpoint(azureConfig.getEndpoint()).credential(new AzureKeyCredential(azureConfig.getApiKey())).buildClient();
        List<ChatRequestMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatRequestSystemMessage("你是一个智能小助手"));
        chatMessages.add(new ChatRequestUserMessage(message));

        ChatCompletions chatCompletionsAnswer = client.getChatCompletions(azureConfig.getModelName(), new ChatCompletionsOptions(chatMessages));
        log.info("请求结果为:{}", JSON.toJSONString(chatCompletionsAnswer));
        String result = chatCompletionsAnswer.getChoices().get(0).getMessage().getContent();
        return result;
    }
}
