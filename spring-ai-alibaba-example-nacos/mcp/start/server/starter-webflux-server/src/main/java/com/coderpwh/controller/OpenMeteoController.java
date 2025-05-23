package com.coderpwh.controller;

import com.coderpwh.service.OpenMeteoService;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

@RestController
public class OpenMeteoController {

    @Resource
    private OpenMeteoService openMeteoService;

    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> sse() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(sequence -> "event: message\ndata: {\"content\": \"Event " + sequence + "\"}\n");
    }

}
