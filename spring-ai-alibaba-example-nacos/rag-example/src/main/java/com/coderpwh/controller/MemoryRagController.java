package com.coderpwh.controller;


import com.coderpwh.service.MemoryRagService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/ai")
public class MemoryRagController {


    private final MemoryRagService memoryRagService;

    public MemoryRagController(MemoryRagService memoryRagService) {
        this.memoryRagService = memoryRagService;
    }


    @GetMapping("/memory")
    public Flux<String> getMemory(@RequestBody Map<String, String> map) {
        String message = map.get("message");
        return memoryRagService.getMemory(message);
    }

}
