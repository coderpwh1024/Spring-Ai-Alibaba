package com.coderpwh.service;

import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

/**
 * @author coderpwh
 */
public interface AzureOpenAiService {


    /***
     *
     * @param message
     * @return
     */
    public String chat(String message);



    public String importData();



}
