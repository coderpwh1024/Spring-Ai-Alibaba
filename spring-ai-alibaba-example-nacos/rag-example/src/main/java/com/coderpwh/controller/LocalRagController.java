package com.coderpwh.controller;

import com.coderpwh.service.RagService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @author coderpwh
 */
@RestController
@RequestMapping("/ai")
public class LocalRagController {

    private final RagService localRagService;

    public LocalRagController(RagService localRagService) {
        this.localRagService = localRagService;
    }

    @GetMapping("/rag/importDocument")
    public void importDocument() {
        localRagService.importDocuments();
    }

    @GetMapping("/rag")
    public Flux<String> generate(@RequestParam(value = "message",
            defaultValue = "how to get start with spring ai alibaba?") String message) {
        return localRagService.retrieve(message).map(x -> x.getResult().getOutput().getContent());
    }


}
