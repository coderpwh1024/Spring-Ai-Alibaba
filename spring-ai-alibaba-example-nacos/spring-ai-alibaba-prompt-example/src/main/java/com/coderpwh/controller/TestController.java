package com.coderpwh.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author coderpwh
 */
@RequestMapping("/test")
@RestController
public class TestController {



    @RequestMapping(value = "/a",method = RequestMethod.GET)
    public String test(){
        return "succeed";
    }

}
