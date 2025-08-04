package com.coderpwh.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author coderpwh
 */

@Data
public class User implements Serializable {

    private Long id;

    private String name;

    private String email;

    private LocalDateTime createdAt;


    // 构造函数
    public User() {}

    public User(String name, String email) {
        this.name = name;
        this.email = email;
        this.createdAt = LocalDateTime.now();
    }



}
