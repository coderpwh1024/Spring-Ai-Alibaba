package com.coderpwh.work;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.util.Assert;

import java.util.Map;

public class RoutingWorkflow {

    private final ChatClient chatClient;

    public RoutingWorkflow(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String route(String input, Map<String, String> routes) {
        Assert.notNull(input, "Input text cannot be null");
        Assert.notEmpty(routes, "Routes map cannot be null or empty");


        String routeKey = determineRoute(input, routes.keySet());
        String selectedPrompt = routes.get(routeKey);

        if (selectedPrompt == null) {
            throw new IllegalArgumentException("Selected route '" + routeKey + "' not found in routes map");
        }

        System.out.println("回答信息:");
        return chatClient.prompt(selectedPrompt + "\nInput: " + input).call().content();
    }


    @SuppressWarnings("null")
    private String determineRoute(String input, Iterable<String> availableRoutes) {
        System.out.println("\n有效的路由: " + availableRoutes);

        String selectorPrompt = String.format("""
                请分析以下输入内容，并从以下支持团队中选择最合适的一个：%s
                请先解释你的判断依据，然后按照以下 JSON 格式提供你的选择：
                \\{
                    "reasoning": "简要说明为何将该请求分配给该支持团队。
                                 请考虑关键词、用户意图以及紧急程度等因素。",
                    "selection": "所选择的团队名称"
                \\}
                输入：%s
                """, availableRoutes, input);


        RoutingResponse routingResponse = chatClient.prompt(selectorPrompt).call().entity(RoutingResponse.class);

        System.out.println(String.format("路由分析:%s\n选中的路由: %s",
                routingResponse.reasoning(), routingResponse.selection()));
        System.out.println();
        return routingResponse.selection();
    }


}
