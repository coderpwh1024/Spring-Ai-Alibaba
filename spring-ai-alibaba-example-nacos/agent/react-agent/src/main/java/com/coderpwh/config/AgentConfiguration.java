package com.coderpwh.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.hip.HumanInTheLoopHook;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.coderpwh.interceptor.LogToolInterceptor;
import com.coderpwh.tools.FileReadTool;
import com.coderpwh.tools.FileWriteTool;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author coderpwh
 */
@Configuration
public class AgentConfiguration {


    private final ChatModel chatModel;


    public AgentConfiguration(ChatModel chatModel) {
        this.chatModel = chatModel;
    }


    @Bean
    public ReactAgent reactAgent() throws GraphStateException {
        return ReactAgent
                .builder()
                .name("agent")
                .description("This is a react agent")
                .model(chatModel)
                .saver(new MemorySaver())
                .tools(
                        new FileReadTool().toolCallback(),
                        new FileWriteTool().toolCallback()
                ).hooks(HumanInTheLoopHook.builder().approvalOn("file_write", "Write File should be approved")
                        .build())
                .interceptors(new LogToolInterceptor())
                .build();
    }


}
