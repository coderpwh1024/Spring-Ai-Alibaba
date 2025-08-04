package com.example.rag.repository;

import com.example.rag.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    
    Optional<Conversation> findBySessionId(String sessionId);
    
    List<Conversation> findByUserIdOrderByUpdatedAtDesc(String userId);
    
    @Query("SELECT c FROM Conversation c WHERE c.userId = :userId AND c.updatedAt >= :fromDate ORDER BY c.updatedAt DESC")
    List<Conversation> findRecentConversationsByUserId(@Param("userId") String userId, @Param("fromDate") java.time.LocalDateTime fromDate);
    
    void deleteBySessionId(String sessionId);
}