package com.example.rag.controller;

import com.example.rag.dto.ApiResponse;
import com.example.rag.dto.DocumentUploadRequest;
import com.example.rag.model.Document;
import com.example.rag.service.RAGService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*")
public class DocumentController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

    @Autowired
    private RAGService ragService;

    @PostMapping("/upload")
    public CompletableFuture<ResponseEntity<ApiResponse<String>>> uploadDocument(
            @Valid @RequestBody DocumentUploadRequest request) {
        
        logger.info("Received document upload request for source: {}", request.getSource());
        
        return ragService.processDocument(request.getContent(), request.getSource())
            .thenApply(documentId -> {
                logger.info("Document processed successfully with ID: {}", documentId);
                return ResponseEntity.ok(ApiResponse.success("Document uploaded and processed successfully", documentId));
            })
            .exceptionally(throwable -> {
                logger.error("Error uploading document: ", throwable);
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to upload document: " + throwable.getMessage()));
            });
    }

    @GetMapping("/search")
    public CompletableFuture<ResponseEntity<ApiResponse<List<Document>>>> searchDocuments(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        
        logger.info("Searching documents with query: {}", query);
        
        return ragService.searchDocuments(query)
            .thenApply(documents -> {
                logger.info("Found {} documents for query: {}", documents.size(), query);
                return ResponseEntity.ok(ApiResponse.success(documents));
            })
            .exceptionally(throwable -> {
                logger.error("Error searching documents: ", throwable);
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to search documents: " + throwable.getMessage()));
            });
    }

    @GetMapping("/{documentId}")
    public CompletableFuture<ResponseEntity<ApiResponse<Document>>> getDocument(
            @PathVariable String documentId) {
        
        logger.info("Retrieving document with ID: {}", documentId);
        
        return ragService.getDocument(documentId)
            .thenApply(document -> {
                if (document != null) {
                    logger.info("Document found: {}", documentId);
                    return ResponseEntity.ok(ApiResponse.success(document));
                } else {
                    logger.warn("Document not found: {}", documentId);
                    return ResponseEntity.notFound().build();
                }
            })
            .exceptionally(throwable -> {
                logger.error("Error retrieving document: ", throwable);
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve document: " + throwable.getMessage()));
            });
    }

    @DeleteMapping("/{documentId}")
    public CompletableFuture<ResponseEntity<ApiResponse<String>>> deleteDocument(
            @PathVariable String documentId) {
        
        logger.info("Deleting document with ID: {}", documentId);
        
        return ragService.deleteDocument(documentId)
            .thenApply(deleted -> {
                if (deleted) {
                    logger.info("Document deleted successfully: {}", documentId);
                    return ResponseEntity.ok(ApiResponse.success("Document deleted successfully"));
                } else {
                    logger.warn("Document not found for deletion: {}", documentId);
                    return ResponseEntity.notFound().build();
                }
            })
            .exceptionally(throwable -> {
                logger.error("Error deleting document: ", throwable);
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete document: " + throwable.getMessage()));
            });
    }
}