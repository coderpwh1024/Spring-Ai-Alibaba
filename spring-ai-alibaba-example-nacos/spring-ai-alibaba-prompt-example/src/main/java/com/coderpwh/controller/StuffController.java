package com.coderpwh.controller;


import com.coderpwh.entity.Completion;
import org.checkerframework.checker.units.qual.C;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author coderpwh
 */
@RestController
@RequestMapping("/example/ai")
public class StuffController {


    @Value("classpath:/docs/wikipedia-curling.md")
    private Resource docsToStuffResource;

    @Value("classpath:/prompts/qa-prompt.st")
    private Resource qaPromptResource;


    @jakarta.annotation.Resource
    private ChatClient chatClient;


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
        } else {
            map.put("context", "");
        }
        Prompt prompt = promptTemplate.create(map);
       String result =    chatClient.call(prompt.getContents());

       return  new Completion(result);

    /*    return new Completion(
                chatClient.prompt(promptTemplate.create(map))
                        .call(promptTemplate.create(map))
                        .content()


        );*/
    }


}
