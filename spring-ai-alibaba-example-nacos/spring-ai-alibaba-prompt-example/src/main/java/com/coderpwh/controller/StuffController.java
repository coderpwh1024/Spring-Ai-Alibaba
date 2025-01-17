package com.coderpwh.controller;

import java.util.HashMap;
import java.util.Map;


import com.coderpwh.entity.Completion;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author coderpwh
 */

@RestController
@RequestMapping("/example/ai")
public class StuffController {

    private final ChatClient chatClient;

    @Value("classpath:/docs/wikipedia-curling.md")
    private Resource docsToStuffResource;

    @Value("classpath:/prompts/qa-prompt.st")
    private Resource qaPromptResource;

    @Autowired
    public StuffController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping(value = "/stuff")
    public Completion completion(
            @RequestParam(value = "message",
                    defaultValue = "Which athletes won the mixed doubles gold medal in curling at the 2022 Winter Olympics?'") String message,
            @RequestParam(value = "stuffit", defaultValue = "false") boolean stuffit
    ) {

        PromptTemplate promptTemplate = new PromptTemplate(qaPromptResource);

        Map<String, Object> map = new HashMap<>();
        map.put("question", message);
        if (stuffit) {
            map.put("context", docsToStuffResource);
        }
        else {
            map.put("context", "");
        }

        return new Completion(
                chatClient.prompt(promptTemplate.create(map))
                        .call()
                        .content()
        );
    }

}
