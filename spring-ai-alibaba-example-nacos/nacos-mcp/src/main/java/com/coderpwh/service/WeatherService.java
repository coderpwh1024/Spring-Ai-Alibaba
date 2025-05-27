package com.coderpwh.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
/**
 * @author coderpwh
 */
@Service
public class WeatherService {

    @Tool(description = "Get weather information by city name")
    public String getWeather(String cityName) {
        return "Sunny in " + cityName;
    }

}
