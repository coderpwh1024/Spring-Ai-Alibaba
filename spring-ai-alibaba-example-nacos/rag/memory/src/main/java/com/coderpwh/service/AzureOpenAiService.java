package com.coderpwh.service;

import com.coderpwh.entity.request.VectorStoreRequestDTO;
import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

import java.util.List;

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


    /***
     * 导入数据
     * @return
     */
    String importVectorData(List<VectorStoreRequestDTO> list);



}
