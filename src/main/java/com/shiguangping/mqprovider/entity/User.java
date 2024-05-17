package com.shiguangping.mqprovider.entity;


import lombok.Data;

@Data
public class User {
    // redis存储id
    private String id;
    // 消息体
    private String message;

}
