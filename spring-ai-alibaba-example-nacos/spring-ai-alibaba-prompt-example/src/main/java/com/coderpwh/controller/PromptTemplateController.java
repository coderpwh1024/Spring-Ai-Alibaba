package com.coderpwh.controller;

import com.alibaba.cloud.ai.prompt.ConfigurablePromptTemplate;
import com.alibaba.cloud.ai.prompt.ConfigurablePromptTemplateFactory;
import opennlp.tools.util.StringUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.io.Resource;

import java.util.Map;

/**
 * @author coderpwh
 */
@RequestMapping("/example/ai")
@RestController
public class PromptTemplateController {


    @Value("classpath:/prompts/joke-prompt.st")
    private Resource jokeResource;

    private final ChatClient chatClient;


    private final ConfigurablePromptTemplateFactory configurablePromptTemplateFactory;

    public PromptTemplateController(ChatClient.Builder builder, ConfigurablePromptTemplateFactory configurablePromptTemplateFactory) {

        this.chatClient = builder.build();
        this.configurablePromptTemplateFactory = configurablePromptTemplateFactory;
    }


    /***
     * 提示词
     * @param adjective
     * @param topic
     * @return
     */
    @GetMapping("/prompt")
    public AssistantMessage completion(@RequestParam(value = "adjective", defaultValue = "funny") String adjective, @RequestParam(value = "topic", defaultValue = "cows") String topic) {

        PromptTemplate promptTemplate = new PromptTemplate(jokeResource);
        Prompt prompt = promptTemplate.create(Map.of("adjective", adjective, "topic", topic));

        return chatClient.prompt(prompt).call().chatResponse().getResult().getOutput();
    }


    /***
     * 提示词模板
     * @param author
     * @return
     */
    @GetMapping("/prompt-template")
    public AssistantMessage generate(@RequestParam(value = "author", defaultValue = "鲁迅") String author) {
        ConfigurablePromptTemplate template = configurablePromptTemplateFactory.getTemplate("test-template");

        if (template != null) {
            template = configurablePromptTemplateFactory.create("test-template",
                    "please list the three most famous books by {author}.");
        }

        Prompt prompt;
        if (StringUtils.hasText(author)) {
            prompt = template.create(Map.of("author", author));
        } else {
            prompt = template.create();
        }

        return chatClient.prompt(prompt).call().chatResponse().getResult().getOutput();
    }


}
