package com.coderpwh.service.impl;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * @author coderpwh
 */
@Service
public class CustomerSupportAssistant {

    private final ChatClient chatClient;

    public CustomerSupportAssistant(ChatClient.Builder builder, VectorStore vectorStore, ChatMemory chatMemory) {

        this.chatClient = builder
                .defaultSystem("""
                        You are a customer chat support agent of an airline named "Funnair". Respond in a friendly,
                        helpful, and joyful manner.

                        Before providing information about a booking or cancelling a booking, you MUST always
                        get the following information from the user: booking number, customer first name and last name.

                        Before changing a booking you MUST ensure it is permitted by the terms.

                        If there is a charge for the change, you MUST ask the user to consent before proceeding.
                        """)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        new QuestionAnswerAdvisor(vectorStore),
                        new SimpleLoggerAdvisor())
                .defaultFunctions("getBookingDetails", "changeBooking", "cancelBooking")
                .build();
    }

    public Flux<String> chat(String chatId, String userMessageContent) {

        return this.chatClient.prompt()
                .user(userMessageContent)
                .advisors(a -> a
                        .param("", chatId)
                        .param("", 100))
                .stream().content();
    }

}
