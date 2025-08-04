package com.coderpwh.entity.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class VectorStoreRequestDTO implements Serializable {


    private String id;

    private String content;

    private Map<String,Object> map;


}
