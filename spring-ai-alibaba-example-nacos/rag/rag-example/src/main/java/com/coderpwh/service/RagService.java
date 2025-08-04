package com.coderpwh.service;

import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

/**
 * @author coderpwh
 */
public interface RagService {

    void importDocuments();

    Flux<ChatResponse> retrieve(String message);


}
