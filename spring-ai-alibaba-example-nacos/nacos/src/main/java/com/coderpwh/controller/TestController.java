package com.coderpwh.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/test")
@RestController
public class TestController {





    @RequestMapping(value = "/test",method = RequestMethod.GET)
    public String test(){
        return "succeed";
    }

}
