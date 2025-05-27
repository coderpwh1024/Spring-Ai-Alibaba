package com.coderpwh.io.modelcontextprotocol.spec;

import com.fasterxml.jackson.core.type.TypeReference;
import io.modelcontextprotocol.spec.McpSession;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/***
 *MCP 会话
 *
 */
@Slf4j
public class McpClientSession implements McpSession {




    @Override
    public <T> Mono<T> sendRequest(String method, Object requestParams, TypeReference<T> typeRef) {
        return null;
    }

    @Override
    public Mono<Void> sendNotification(String method, Object params) {
        return null;
    }

    @Override
    public Mono<Void> closeGracefully() {
        return null;
    }

    @Override
    public void close() {

    }
}
