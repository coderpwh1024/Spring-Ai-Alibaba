package com.coderpwh.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

public interface MemoryRagService {


    Flux<String> getMemory(String message);

}
