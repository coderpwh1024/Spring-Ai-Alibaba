package com.example.rag.controller;

import com.example.rag.dto.ApiResponse;
import com.example.rag.model.Conversation;
import com.example.rag.model.ConversationMessage;
import com.example.rag.repository.ConversationRepository;
import com.example.rag.repository.ConversationMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * @author coderpwh
 */
@RestController
@RequestMapping("/api/conversations")
@CrossOrigin(origins = "*")
public class ConversationController {

    private static final Logger logger = LoggerFactory.getLogger(ConversationController.class);

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ConversationMessageRepository messageRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Conversation>>> getAllConversations(
            @RequestParam(required = false) String userId) {
        
        logger.info("Retrieving conversations for user: {}", userId);
        
        List<Conversation> conversations;
        if (userId != null) {
            conversations = conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId);
        } else {
            conversations = conversationRepository.findAll();
        }
        
        return ResponseEntity.ok(ApiResponse.success(conversations));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<Conversation>> getConversation(@PathVariable String sessionId) {
        logger.info("Retrieving conversation: {}", sessionId);
        
        Optional<Conversation> conversation = conversationRepository.findBySessionId(sessionId);
        
        if (conversation.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(conversation.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{sessionId}/messages")
    public ResponseEntity<ApiResponse<List<ConversationMessage>>> getConversationMessages(
            @PathVariable String sessionId) {
        
        logger.info("Retrieving messages for conversation: {}", sessionId);
        
        List<ConversationMessage> messages = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<String>> deleteConversation(@PathVariable String sessionId) {
        logger.info("Deleting conversation: {}", sessionId);
        
        Optional<Conversation> conversation = conversationRepository.findBySessionId(sessionId);
        
        if (conversation.isPresent()) {
            conversationRepository.deleteBySessionId(sessionId);
            return ResponseEntity.ok(ApiResponse.success("Conversation deleted successfully"));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}