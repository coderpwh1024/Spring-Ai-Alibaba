package com.coderpwh.controller;

import com.coderpwh.service.OpenMeteoService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OpenMeteoController {

    @Resource
    private OpenMeteoService openMeteoService;

    @RequestMapping(value = "/see", method = RequestMethod.GET)
    public Object getWeatherInfo() {
        String str = "这是天气";

        return str;
    }

}
