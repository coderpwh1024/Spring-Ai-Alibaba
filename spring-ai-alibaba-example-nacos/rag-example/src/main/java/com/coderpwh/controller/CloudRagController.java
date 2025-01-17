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
public class CloudRagController {


    private final RagService cloudRagService;

    public CloudRagController(RagService cloudRagService) {
        this.cloudRagService = cloudRagService;
    }

    @GetMapping("/cloud/rag/importDocument")
    public void importDocument() {
        cloudRagService.importDocuments();
    }

    @GetMapping("/cloud/rag")
    public Flux<String> generate(@RequestParam(value = "message",
            defaultValue = "how to get start with spring ai alibaba?") String message) {
        return cloudRagService.retrieve(message).map(x -> x.getResult().getOutput().getContent());
    }

}
