package com.tzy.pojo;

import lombok.Data;

@Data
public class ChatMessage {
    private String message;  // 用户消息
    private Integer type;    // AI类型
    private String response; // AI响应
    private Long timestamp; // 时间戳
    
    // 构造函数
    public ChatMessage() {
        this.timestamp = System.currentTimeMillis();
    }
} 