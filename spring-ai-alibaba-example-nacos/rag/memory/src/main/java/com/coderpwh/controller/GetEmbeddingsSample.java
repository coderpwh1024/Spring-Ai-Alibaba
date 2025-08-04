package com.coderpwh.controller;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.EmbeddingItem;
import com.azure.ai.openai.models.Embeddings;
import com.azure.ai.openai.models.EmbeddingsOptions;
import com.azure.ai.openai.models.EmbeddingsUsage;
import com.azure.core.credential.AzureKeyCredential;

import java.util.Arrays;

/**
 * @author coderpwh
 */
public class GetEmbeddingsSample {

    public static void main(String[] args) {
        String azureOpenaiKey = "";
        String endpoint = "";
        String deploymentOrModelId = "text-embedding-3-large";

        OpenAIClient client = new OpenAIClientBuilder()
                .endpoint(endpoint)
                .credential(new AzureKeyCredential(azureOpenaiKey))
                .buildClient();

        EmbeddingsOptions embeddingsOptions = new EmbeddingsOptions(Arrays.asList("Your text string goes here"));

        Embeddings embeddings = client.getEmbeddings(deploymentOrModelId, embeddingsOptions);

        for (EmbeddingItem item : embeddings.getData()) {
            System.out.printf("Index: %d.%n", item.getPromptIndex());
            System.out.println("Embedding as base64 encoded string: " + item.getEmbeddingAsString());
            System.out.println("Embedding as list of floats: ");
            for (Float embedding : item.getEmbedding()) {
                System.out.printf("%f;", embedding);
            }
        }

        EmbeddingsUsage usage = embeddings.getUsage();
        System.out.printf(
                "Usage: number of prompt token is %d and number of total tokens in request and response is %d.%n",
                usage.getPromptTokens(), usage.getTotalTokens());
    }
}
