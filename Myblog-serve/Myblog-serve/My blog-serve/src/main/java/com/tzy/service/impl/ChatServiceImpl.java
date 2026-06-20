package com.tzy.service.impl;

import com.tzy.enums.AIType;
import com.tzy.service.ChatService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class ChatServiceImpl implements ChatService {

    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${openrouter.api.key}")
    private String apiKey;
    @Value("${openrouter.api.url:https://openrouter.ai/api/v1/chat/completions}")
    private String apiUrl;

    @Override
    public String chat(String message, Integer type) {
        // 获取对应的AI类型
        AIType aiType = AIType.getByCode(type);
        
        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("HTTP-Referer", "https://your-app-url.com"); // 替换为你的应用URL
        
        // 构建消息列表
        List<Map<String, String>> messages = new ArrayList<>();
        
        // 根据AI类型添加不同的系统提示词
        switch (aiType) {
            case GENERAL:
                messages.add(Map.of("role", "system", 
                    "content", "你是一个通用AI助手,可以帮助用户解答各种问题。请用专业、友善的语气回答。"));
                break;
            case GIRLFRIEND:
                messages.add(Map.of("role", "system", 
                    "content", "你是一个温柔体贴的女友,说话要温柔可爱,多用可爱的语气词和表情。要像恋人一样和用户互动。"));
                break;
            case FIREFLY:
                messages.add(Map.of("role", "system", 
                    "content", "你是一个名叫流萤的AI,性格活泼开朗,说话俏皮可爱。要用轻松愉快的语气和用户交谈。"));
                break;
        }
        
        // 添加用户消息
        messages.add(Map.of("role", "user", "content", message));
        
        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", aiType.getModelId());
        requestBody.put("messages", messages);
        
        // 发送请求
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);
        
        // 解析响应
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (!choices.isEmpty()) {
                Map<String, Object> message_obj = (Map<String, Object>) choices.get(0).get("message");
                return (String) message_obj.get("content");
            }
        }
        
        throw new RuntimeException("Failed to get response from AI");
    }

    @Override
    public List<Map<String, Object>> getAITypes() {
        List<Map<String, Object>> types = new ArrayList<>();
        for (AIType type : AIType.values()) {
            Map<String, Object> typeInfo = new HashMap<>();
            typeInfo.put("code", type.getCode());
            typeInfo.put("name", type.getName());
            typeInfo.put("modelId", type.getModelId());
            types.add(typeInfo);
        }
        return types;
    }
} 