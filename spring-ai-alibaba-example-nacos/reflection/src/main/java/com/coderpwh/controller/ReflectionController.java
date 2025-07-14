package com.coderpwh.controller;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.agent.ReflectAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * @author coderpwh
 */
@RequestMapping("/reflection")
@RestController
public class ReflectionController {

    private static final Logger logger = LoggerFactory.getLogger(ReflectionController.class);

    private CompiledGraph compiledGraph;

    public ReflectionController(@Qualifier("reflectGraph") CompiledGraph compiledGraph) {
        this.compiledGraph = compiledGraph;
    }

    @GetMapping("/chat")
    public String simpleChat(String query) throws GraphRunnerException {
        return compiledGraph.invoke(Map.of(ReflectAgent.MESSAGES, List.of(new UserMessage(query))))
                .get()
                .<List<Message>>value(ReflectAgent.MESSAGES)
                .orElseThrow()
                .stream()
                .filter(message -> message.getMessageType() == MessageType.ASSISTANT)
                .reduce((first, second) -> second)
                .map(Message::getText)
                .orElseThrow();
    }

}
