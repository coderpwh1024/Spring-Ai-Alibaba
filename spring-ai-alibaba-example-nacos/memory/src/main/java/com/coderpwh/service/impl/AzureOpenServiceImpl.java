package com.coderpwh.service.impl;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsJsonResponseFormat;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.core.credential.AzureKeyCredential;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author coderpwh
 */
@Slf4j
@Service
public class AzureOpenServiceImpl {


    public void test(){

        OpenAIClient client = new OpenAIClientBuilder().endpoint("").credential(new AzureKeyCredential("")).buildClient();
        List<ChatRequestMessage> chatMessages = new ArrayList<>();
        ChatCompletionsJsonResponseFormat format = new ChatCompletionsJsonResponseFormat();
        ChatCompletions chatCompletionsAnswer = client.getChatCompletions("", new ChatCompletionsOptions(chatMessages).setResponseFormat(format));

    }

}
