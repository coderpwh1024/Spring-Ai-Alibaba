package com.coderpwh.service.impl;

import com.coderpwh.service.MemoryRagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@Service
public class MemoryRagServiceImpl implements MemoryRagService {


    /***
     * 存储
     * @param message
     * @return
     */
    @Override
    public Flux<String> getMemory(String message) {
        return null;
    }


}
