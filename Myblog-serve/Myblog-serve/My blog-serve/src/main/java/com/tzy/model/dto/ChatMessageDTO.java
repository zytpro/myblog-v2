package com.tzy.model.dto;

import lombok.Data;

/**
 * AI 聊天消息请求 DTO
 * @author tzy
 * @since 2024.2.8
 */
@Data
public class ChatMessageDTO {
    /**
     * 用户发送的消息内容
     */
    private String message;

    /**
     * AI 角色代码
     * 可选值: general, programmer, writer, translator, teacher
     * 默认为 general (通用助手)
     */
    private String role = "general";
}
