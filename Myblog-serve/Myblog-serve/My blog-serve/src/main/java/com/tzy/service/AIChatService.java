package com.tzy.service;

import com.tzy.config.Logger;
import com.tzy.config.OpenRouterConfig;
import com.tzy.model.dto.ChatRequest;
import com.tzy.model.dto.OpenRouterResponse;
import com.tzy.model.enums.AIRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 聊天服务
 * 负责与 OpenRouter API 交互，处理不同角色的 AI 对话
 * @author tzy
 * @since 2024.2.8
 */
@Service
public class AIChatService {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OpenRouterConfig config;

    /**
     * 发送消息给 AI 助手并获取回复
     * @param message 用户消息
     * @param roleCode AI 角色代码
     * @return AI 的回复内容
     */
    public String chat(String message, String roleCode) {
        AIRole role = AIRole.getByCode(roleCode);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + config.getKey());
        headers.set("HTTP-Referer", "https://myblog.com");
        headers.set("X-Title", "My Blog AI Chat");

        ChatRequest request = new ChatRequest();
        request.setModel(config.getModel());

        List<ChatRequest.Message> messages = new ArrayList<>();

        // 添加角色系统提示语
        ChatRequest.Message systemMessage = new ChatRequest.Message();
        systemMessage.setRole("system");
        systemMessage.setContent(role.getPrompt());
        messages.add(systemMessage);

        // 添加用户消息
        ChatRequest.Message userMessage = new ChatRequest.Message();
        userMessage.setRole("user");
        userMessage.setContent(message);
        messages.add(userMessage);

        request.setMessages(messages);

        HttpEntity<ChatRequest> entity = new HttpEntity<>(request, headers);

        try {
            Logger.info("Sending request to OpenRouter API with role: " + role.getName());
            Logger.info("Request URL: " + config.getUrl());
            Logger.info("Request Headers: " + headers);
            Logger.info("Request Body: " + request);

            OpenRouterResponse response = restTemplate.postForObject(
                config.getUrl(),
                entity,
                OpenRouterResponse.class
            );

            Logger.info("Response from OpenRouter API: " + response);

            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                return response.getChoices().get(0).getMessage().getContent();
            }
            return "抱歉，AI 暂时无法回答您的问题，请稍后再试。";
        } catch (Exception e) {
            Logger.error("AI Chat Error: " + e.getMessage());
            Logger.error("Error details: " + e.toString() + "\n" +
                        "Stack trace: " + String.join("\n",
                            java.util.Arrays.stream(e.getStackTrace())
                                .map(StackTraceElement::toString)
                                .limit(5)
                                .toArray(String[]::new)));
            return "系统出现错误: " + e.getMessage();
        }
    }
}
