package com.coderpwh.controller;

import com.coderpwh.entity.User;
import com.coderpwh.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @author coderpwh
 */
@RequestMapping("/api/users")
@RestController
public class UserController {


    @Resource
    private UserService userService;


    @GetMapping
    public Flux<User> getAllUsers() {
     return  Flux.empty();
    }


}
