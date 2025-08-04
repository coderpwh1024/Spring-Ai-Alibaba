package com.example.rag.repository;

import com.example.rag.model.ConversationMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, Long> {
    
    List<ConversationMessage> findByConversationIdOrderByCreatedAtAsc(Long conversationId);
    
    @Query("SELECT cm FROM ConversationMessage cm WHERE cm.conversation.sessionId = :sessionId ORDER BY cm.createdAt ASC")
    List<ConversationMessage> findBySessionIdOrderByCreatedAtAsc(@Param("sessionId") String sessionId);
    
    @Query("SELECT cm FROM ConversationMessage cm WHERE cm.conversation.id = :conversationId ORDER BY cm.createdAt DESC LIMIT :limit")
    List<ConversationMessage> findRecentMessagesByConversationId(@Param("conversationId") Long conversationId, @Param("limit") int limit);
}