package com.example.rag.service;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class AzureOpenAIService {

    private static final Logger logger = LoggerFactory.getLogger(AzureOpenAIService.class);

    @Autowired
    private OpenAIAsyncClient openAIAsyncClient;

    @Value("${azure.openai.deployment-name}")
    private String deploymentName;

    public CompletableFuture<String> generateCompletion(String prompt) {
        return generateCompletion(prompt, 0.7, 1000);
    }

    public CompletableFuture<String> generateCompletion(String prompt, double temperature, int maxTokens) {
        ChatCompletionsOptions options = new ChatCompletionsOptions(
                List.of(new ChatRequestUserMessage(prompt)))
                .setTemperature(temperature)
                .setMaxTokens(maxTokens);

        return openAIAsyncClient.getChatCompletions(deploymentName, options)
                .toFuture()
                .thenApply(completions -> {
                    if (completions.getChoices() != null && !completions.getChoices().isEmpty()) {
                        return completions.getChoices().get(0).getMessage().getContent();
                    }
                    return "";
                })
                .exceptionally(throwable -> {
                    logger.error("Error generating completion: ", throwable);
                    return "Error generating response";
                });
    }

    public CompletableFuture<String> generateEmbedding(String text) {
        EmbeddingsOptions embeddingsOptions = new EmbeddingsOptions(List.of(text));
        
        return openAIAsyncClient.getEmbeddings("text-embedding-ada-002", embeddingsOptions)
                .toFuture()
                .thenApply(embeddings -> {
                    if (embeddings.getData() != null && !embeddings.getData().isEmpty()) {
                        List<Float> embedding = embeddings.getData().get(0).getEmbedding();
                        return embedding.toString();
                    }
                    return "";
                })
                .exceptionally(throwable -> {
                    logger.error("Error generating embedding: ", throwable);
                    return "";
                });
    }

    public CompletableFuture<String> generateWithFunctions(String prompt, List<ChatCompletionsFunctionToolDefinition> functions) {
        ChatCompletionsOptions options = new ChatCompletionsOptions(
                List.of(new ChatRequestUserMessage(prompt)))
                .setTemperature(0.7)
                .setMaxTokens(1000);

        if (functions != null && !functions.isEmpty()) {
            options.setTools(functions.stream().map(f -> (ChatCompletionsToolDefinition) f).toList());
        }

        return openAIAsyncClient.getChatCompletions(deploymentName, options)
                .toFuture()
                .thenApply(completions -> {
                    if (completions.getChoices() != null && !completions.getChoices().isEmpty()) {
                        ChatResponseMessage message = completions.getChoices().get(0).getMessage();
                        
                        if (message.getToolCalls() != null && !message.getToolCalls().isEmpty()) {
                            return handleToolCalls(message.getToolCalls());
                        }
                        
                        return message.getContent();
                    }
                    return "";
                })
                .exceptionally(throwable -> {
                    logger.error("Error generating completion with functions: ", throwable);
                    return "Error generating response";
                });
    }

    private String handleToolCalls(List<ChatCompletionsToolCall> toolCalls) {
        StringBuilder result = new StringBuilder();
        
        for (ChatCompletionsToolCall toolCall : toolCalls) {
            if (toolCall instanceof ChatCompletionsFunctionToolCall functionCall) {
                String functionName = functionCall.getFunction().getName();
                String arguments = functionCall.getFunction().getArguments();
                
                result.append("Function call: ").append(functionName)
                      .append(" with arguments: ").append(arguments).append("\n");
            }
        }
        
        return result.toString();
    }
}