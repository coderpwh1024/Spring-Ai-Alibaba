package com.coderpwh.entity.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class ElasticSearchRequestDTO implements Serializable {


    @JsonProperty("id")
    private String id;

    @JsonProperty("content")
    private String content;

    @JsonProperty("metadata")
    private MetadataRequestDTO metadata;
}
