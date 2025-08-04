package com.coderpwh.controller;

import com.coderpwh.entity.User;
import com.coderpwh.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

/**
 * @author coderpwh
 */
@Slf4j
@RequestMapping("/api/users/")
@RestController
public class UserController {


    @Resource
    private UserService userService;


    @PostMapping(value = "/all")
    public Flux<List<User>> getAllUsers() {
        List<User> list = userService.getAllUsers();
//      return Flux.create(fluxSink -> {
//           fluxSink.next(list);
//       });

        log.info("list:{}", list);
        return Flux.just(list);
    }

    @RequestMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> getStreamText() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(i -> "flux data " + i)
                .log();
    }


}
