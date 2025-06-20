package com.coderpwh.controller;

import com.coderpwh.entity.User;
import com.coderpwh.service.UserService;
import jakarta.annotation.Resource;
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
@RequestMapping("/api/users/")
@RestController
public class UserController {


    @Resource
    private UserService userService;


    @PostMapping(value = "/all")
    public Flux<List<User>> getAllUsers() {
       List<User>  list =  userService.getAllUsers();
      return Flux.create(fluxSink -> {
           fluxSink.next(list);
       });


//       return  Flux.just(list).delaySequence(Duration.ofSeconds(2000));
    }


}
