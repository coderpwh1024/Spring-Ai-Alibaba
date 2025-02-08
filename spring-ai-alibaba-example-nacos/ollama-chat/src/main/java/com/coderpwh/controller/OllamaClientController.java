package com.coderpwh.controller;

import jakarta.servlet.http.HttpServletResponse;
import reactor.core.publisher.Flux;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author coderpwh
 */
@RestController
@RequestMapping("/ollama/chat-client")
public class OllamaClientController {

    private static final String DEFAULT_PROMPT = "你好，介绍下你自己！请用中文回答。";

    private final ChatClient ollamaiChatClient;

    private final ChatModel ollamaiChatModel;

    public OllamaClientController(ChatModel chatModel) {

        this.ollamaiChatModel = chatModel;

        // 构造时，可以设置 ChatClient 的参数
        // {@link org.springframework.ai.chat.client.ChatClient};
        this.ollamaiChatClient = ChatClient.builder(chatModel)
                // 实现 Chat Memory 的 Advisor
                // 在使用 Chat Memory 时，需要指定对话 ID，以便 Spring AI 处理上下文。
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(new InMemoryChatMemory())
                )
                // 实现 Logger 的 Advisor
                .defaultAdvisors(
                        new SimpleLoggerAdvisor()
                )
                // 设置 ChatClient 中 ChatModel 的 Options 参数
                .defaultOptions(
                        OllamaOptions.builder()
                                .withTopP(0.7)
                                .withModel("deepseek-r1:8b")
                                .build()
                )
                .build();
    }

    /**
     * ChatClient 简单调用
     */
    @GetMapping("/simple/chat")
    public String simpleChat() {

        return ollamaiChatClient.prompt(DEFAULT_PROMPT).call().content();
    }

    /**
     * ChatClient 流式调用
     */
    @GetMapping("/stream/chat")
    public Flux<String> streamChat(HttpServletResponse response) {

        response.setCharacterEncoding("UTF-8");
        return ollamaiChatClient.prompt(DEFAULT_PROMPT).stream().content();
    }

}
