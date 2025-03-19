package com.coderpwh.controller;

import com.coderpwh.service.AzureOpenAiService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * @author coderpwh
 */
@RestController
@RequestMapping("/memory")
public class MemoryController {


    @Resource
    private AzureOpenAiService azureOpenAiService;

    @PostMapping("/message")
    public String getMemory(@RequestBody HashMap<String, String> map) {
        String message = map.get("message");
        return azureOpenAiService.chat(message);
    }


    @PostMapping("/index")
    public String getIndex() {
        return azureOpenAiService.importData();
    }

}
