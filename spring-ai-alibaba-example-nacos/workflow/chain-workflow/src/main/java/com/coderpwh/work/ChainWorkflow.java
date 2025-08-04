package com.coderpwh.work;

import org.springframework.ai.chat.client.ChatClient;

/**
 * @author coderpwh
 */
public class ChainWorkflow {

    private static final String[] DEFAULT_SYSTEM_PROMPTS = {
            """
            请分析慧慧开咖啡馆所面临的市场背景、潜在机会和挑战。特别需要关注年轻人和上班族的消费习惯、生活方式与需求偏好。
            """,
            """
            基于上一步对目标人群的分析，列出一个咖啡馆运营的初步方案，包括选址建议、装修风格、产品定位、营业时间和营销策略等。
            """,
            """
            请根据前两个步骤的分析，优化并输出一份完整、可执行的咖啡馆运营方案。请包括核心理念、重点运营策略、成本控制建议和预期效益分析。
            """
    };


    private final ChatClient chatClient;

    private final String[] systemPrompts;

    public ChainWorkflow(ChatClient chatClient) {
        this(chatClient, DEFAULT_SYSTEM_PROMPTS);
    }

    public ChainWorkflow(ChatClient chatClient, String[] systemPrompts) {
        this.chatClient = chatClient;
        this.systemPrompts = systemPrompts;
    }


    public String chain(String userInput) {
        int step = 1;

        String response = userInput;

        for (String prompt : systemPrompts) {
            String input = String.format("{%s}\n {%s}", prompt, response);

            response = chatClient.prompt(input).call().content();

            System.out.println(String.format("\nSTEP %s:\n %s", step++, response));
        }

        return response;
    }


}
