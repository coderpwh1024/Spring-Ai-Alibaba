package com.example.rag.service;

import com.example.rag.model.Document;
import com.example.rag.model.DocumentChunk;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author coderpwh
 */
@Service
public class RAGService {

    private static final Logger logger = LoggerFactory.getLogger(RAGService.class);

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private AzureOpenAIService azureOpenAIService;

    @Autowired
    private DocumentProcessingService documentProcessingService;

    @Value("${rag.max-results:10}")
    private int maxResults;

    @Value("${rag.similarity-threshold:0.7}")
    private double similarityThreshold;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public CompletableFuture<String> processDocument(String content, String source) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String documentId = UUID.randomUUID().toString();
                String title = documentProcessingService.extractTitle(content);
                Map<String, Object> metadata = documentProcessingService.extractMetadata(content, source);

                Document document = new Document(documentId, title, content, source);
                document.setMetadata(metadata);

                List<DocumentChunk> chunks = documentProcessingService.chunkDocument(document);
                document.setChunks(chunks);

                elasticsearchService.indexDocument(document);

                for (DocumentChunk chunk : chunks) {
                    CompletableFuture<String> embeddingFuture = azureOpenAIService.generateEmbedding(chunk.getContent());
                    embeddingFuture.thenAccept(embeddingStr -> {
                        try {
                            List<Double> embedding = parseEmbedding(embeddingStr);
                            chunk.setEmbedding(embedding);
                            elasticsearchService.indexDocumentChunk(chunk);
                        } catch (Exception e) {
                            logger.error("Error processing embedding for chunk {}: ", chunk.getChunkId(), e);
                        }
                    });
                }

                return documentId;
            } catch (Exception e) {
                logger.error("Error processing document: ", e);
                throw new RuntimeException("Failed to process document", e);
            }
        });
    }

    public CompletableFuture<String> query(String question, String sessionId) {
        return retrieveRelevantContext(question)
            .thenCompose(context -> {
                String prompt = buildRAGPrompt(question, context);
                return azureOpenAIService.generateCompletion(prompt);
            })
            .exceptionally(throwable -> {
                logger.error("Error processing query: ", throwable);
                return "I apologize, but I encountered an error while processing your question. Please try again.";
            });
    }

    @Cacheable(value = "ragContext", key = "#question")
    public CompletableFuture<List<DocumentChunk>> retrieveRelevantContext(String question) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Document> keywordResults = elasticsearchService.searchDocuments(question, maxResults);
                
                CompletableFuture<String> embeddingFuture = azureOpenAIService.generateEmbedding(question);
                List<Double> questionEmbedding = parseEmbedding(embeddingFuture.join());
                
                List<DocumentChunk> semanticResults = elasticsearchService.searchSimilarChunks(
                    questionEmbedding, similarityThreshold, maxResults);
                
                Set<String> seenChunkIds = new HashSet<>();
                List<DocumentChunk> combinedResults = new ArrayList<>();
                
                for (DocumentChunk chunk : semanticResults) {
                    if (seenChunkIds.add(chunk.getChunkId())) {
                        combinedResults.add(chunk);
                    }
                }
                
                for (Document doc : keywordResults) {
                    if (doc.getChunks() != null) {
                        for (DocumentChunk chunk : doc.getChunks()) {
                            if (seenChunkIds.add(chunk.getChunkId())) {
                                combinedResults.add(chunk);
                            }
                        }
                    }
                }
                
                return combinedResults.stream()
                    .limit(maxResults)
                    .collect(Collectors.toList());
                    
            } catch (Exception e) {
                logger.error("Error retrieving relevant context: ", e);
                return new ArrayList<>();
            }
        });
    }

    private String buildRAGPrompt(String question, List<DocumentChunk> context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a helpful assistant that answers questions based on the provided context. ");
        prompt.append("Use the context information to provide accurate and relevant answers. ");
        prompt.append("If the context doesn't contain enough information to answer the question, ");
        prompt.append("say so and provide what information you can.\n\n");
        
        prompt.append("Context:\n");
        for (int i = 0; i < context.size(); i++) {
            DocumentChunk chunk = context.get(i);
            prompt.append("Document ").append(i + 1).append(":\n");
            prompt.append(chunk.getContent()).append("\n\n");
        }
        
        prompt.append("Question: ").append(question).append("\n\n");
        prompt.append("Answer:");
        
        return prompt.toString();
    }

    public CompletableFuture<List<Document>> searchDocuments(String query) {
        return CompletableFuture.supplyAsync(() -> 
            elasticsearchService.searchDocuments(query, maxResults));
    }

    public CompletableFuture<Document> getDocument(String documentId) {
        return CompletableFuture.supplyAsync(() -> 
            elasticsearchService.getDocumentById(documentId));
    }

    public CompletableFuture<Boolean> deleteDocument(String documentId) {
        return CompletableFuture.supplyAsync(() -> 
            elasticsearchService.deleteDocument(documentId));
    }

    private List<Double> parseEmbedding(String embeddingStr) {
        try {
            if (embeddingStr == null || embeddingStr.trim().isEmpty()) {
                return new ArrayList<>();
            }
            
            embeddingStr = embeddingStr.trim();
            if (embeddingStr.startsWith("[") && embeddingStr.endsWith("]")) {
                return objectMapper.readValue(embeddingStr, new TypeReference<List<Double>>() {});
            } else {
                String[] parts = embeddingStr.replace("[", "").replace("]", "").split(",");
                return Arrays.stream(parts)
                    .map(String::trim)
                    .map(Double::parseDouble)
                    .collect(Collectors.toList());
            }
        } catch (JsonProcessingException | NumberFormatException e) {
            logger.error("Error parsing embedding: {}", embeddingStr, e);
            return new ArrayList<>();
        }
    }
}