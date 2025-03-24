package com.coderpwh.entity.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Data
public class MetadataRequestDTO implements Serializable {

    @JsonProperty("id")
    private String id;

    @JsonProperty("user_content")
    private String user_content;

    @JsonProperty("user_id")
    private String user_id;


    @JsonProperty("prompt_content")
    private String prompt_content;


    @JsonProperty("create_time")
    private String create_time;

}
