package com.coderpwh.config;

import org.springframework.ai.chat.model.ChatModel;

/**
 * @author coderpwh
 */
public class AgentConfiguration {


     private  final ChatModel chatModel;


     public AgentConfiguration(ChatModel chatModel) {
         this.chatModel = chatModel;
     }








}
