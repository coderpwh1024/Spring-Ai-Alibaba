package com.coderpwh.service.impl;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author coderpwh
 */
@Slf4j
@Service
public class MessageChatMemoryServiceImpl implements ChatMemory {


    /***
     * 添加消息
     * @param conversationId
     * @param messages
     */
    @Override
    public void add(String conversationId, List<Message> messages) {
        log.info("添加消息,conversationId:{},messages:{}", conversationId, JSON.toJSONString(messages));
    }


    /***
     * 查询消息
     * @param conversationId
     * @param lastN
     * @return
     */
    @Override
    public List<Message> get(String conversationId, int lastN) {
        log.info("查询消息,conversationId:{},lastN:{}", conversationId,lastN);

        List<Message> list = new ArrayList<>();
        list.add(new Message() {
            @Override
            public MessageType getMessageType() {
                return MessageType.USER;
            }

            @Override
            public String getText() {
                return "喜欢听周杰伦的歌曲";
            }

            @Override
            public Map<String, Object> getMetadata() {
                Map<String,Object> map = new ConcurrentHashMap<>();
                map.put("用户问:","喜欢听周杰伦的歌曲");
                map.put("助手回答:","喜欢听周杰伦《七里香》《青花瓷等歌曲》");
                return map;
            }
        });
        return list;
    }

    /***
     * 清除消息
     * @param conversationId
     */
    @Override
    public void clear(String conversationId) {
       log.info("清除消息,conversationId:{}", conversationId);
    }
}
