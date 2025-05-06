package com.coderpwh.io.modelcontextprotocol.spec;

import com.fasterxml.jackson.core.type.TypeReference;
import io.modelcontextprotocol.spec.McpSession;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.logging.Logger;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.core.type.TypeReference;
import io.modelcontextprotocol.util.Assert;
import org.slf4j.LoggerFactory;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

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
