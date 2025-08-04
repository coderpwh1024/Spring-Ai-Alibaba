package com.coderpwh.entity.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class VectorStoreMapRequestDTO implements Serializable {

    private String id;

    private String user_id;

    private String user_content;

    private String prompt_content;

    private String create_time;


}
