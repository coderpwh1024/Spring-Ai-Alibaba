package com.example.rag.controller;

import com.example.rag.dto.ApiResponse;
import com.example.rag.dto.QueryRequest;
import com.example.rag.service.AgenticWorkflowService;
import com.example.rag.service.RAGService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author coderpwh
 */
@RestController
@RequestMapping("/api/query")
@CrossOrigin(origins = "*")
public class QueryController {

    private static final Logger logger = LoggerFactory.getLogger(QueryController.class);

    @Autowired
    private RAGService ragService;

    @Autowired
    private AgenticWorkflowService agenticWorkflowService;

    @PostMapping
    public CompletableFuture<ResponseEntity<ApiResponse<String>>> query(
            @Valid @RequestBody QueryRequest request) {
        
        String sessionId = request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString();
        logger.info("Processing query for session {}: {}", sessionId, request.getQuery());
        
        CompletableFuture<String> responseFuture;
        
        if (request.isUseAgentic()) {
            logger.info("Using agentic workflow for query processing");
            responseFuture = agenticWorkflowService.executeAgenticWorkflow(sessionId, request.getQuery());
        } else {
            logger.info("Using standard RAG for query processing");
            responseFuture = ragService.query(request.getQuery(), sessionId);
        }
        
        return responseFuture
            .thenApply(response -> {
                logger.info("Query processed successfully for session: {}", sessionId);
                return ResponseEntity.ok(ApiResponse.success("Query processed successfully", response));
            })
            .exceptionally(throwable -> {
                logger.error("Error processing query for session {}: ", sessionId, throwable);
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to process query: " + throwable.getMessage()));
            });
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<ApiResponse<String>> getSessionInfo(@PathVariable String sessionId) {
        logger.info("Retrieving session info for: {}", sessionId);
        return ResponseEntity.ok(ApiResponse.success("Session ID: " + sessionId));
    }
}