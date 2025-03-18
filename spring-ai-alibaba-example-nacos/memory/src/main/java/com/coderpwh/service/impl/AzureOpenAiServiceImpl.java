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
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.azure.openai.AzureOpenAiChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

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



/*
     @Override
    public String chat(String message) {
//        log.info("azureConfig:{}",JSON.toJSONString(azureConfig));

        OpenAIClient client = new OpenAIClientBuilder()
                .endpoint(azureConfig.getEndpoint())
                .credential(new AzureKeyCredential(azureConfig.getApiKey()))
                .buildClient();

        List<ChatRequestMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatRequestSystemMessage("你是一个智能小助手"));
        chatMessages.add(new ChatRequestUserMessage(message));
        ChatCompletions chatCompletionsAnswer = client.getChatCompletions(azureConfig.getModelName(), new ChatCompletionsOptions(chatMessages));
        log.info("请求结果为:{}", JSON.toJSONString(chatCompletionsAnswer));
        String result = chatCompletionsAnswer.getChoices().get(0).getMessage().getContent();
        return result;
    }
*/


    @Override
    public String chat(String message) {
        var openAIClientBuilder = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(azureConfig.getApiKey()))
                .endpoint(azureConfig.getEndpoint());
        var openAIChatOptions = AzureOpenAiChatOptions.builder()
                .deploymentName(azureConfig.getModelName())
                .build();

        var chatModel = AzureOpenAiChatModel.builder()
                .openAIClientBuilder(openAIClientBuilder)
                .defaultOptions(openAIChatOptions)
                .build();


        List<Advisor> advisorsList = new ArrayList<>();
        advisorsList.add(new PromptChatMemoryAdvisor(new InMemoryChatMemory(), "你是一个智能小助手,回答用户所有的提问，要求风格幽默有趣"));

        ChatClient chatClient = ChatClient.builder(chatModel).defaultAdvisors(advisorsList).build();

        ChatResponse chatResponse = chatClient.prompt().user(message).call().chatResponse();
        log.info("callResponseSpec:{}", JSON.toJSONString(chatResponse));
        String text = chatResponse.getResult().getOutput().getText();

        return text;
    }


}
