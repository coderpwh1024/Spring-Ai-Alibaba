package com.coderpwh.service;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class DogService {


    /***
     *  在线生成小狗照片
     */
    private static final String BASE_URL = "https://dog.ceo/api/breeds/image/random";

    private final RestClient restClient;


    /***
     * 构造函数
     */
    public DogService() {
        this.restClient = RestClient.builder()
                .build();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Dog {
        private String message;

        private String status;


        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }


    @Tool(description = "获取小狗照片信息")
    public String getDogInfo() {
        var dogData = restClient.get().uri(BASE_URL).retrieve().body(Dog.class);
        log.info("结果为:{}", JSON.toJSONString(dogData));
        return JSON.toJSONString(dogData);
    }


    public static void main(String[] args) {
        DogService client = new DogService();
        String result = client.getDogInfo();
        System.out.println(result);
    }
}
