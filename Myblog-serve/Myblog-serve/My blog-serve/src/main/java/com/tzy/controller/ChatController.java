package com.tzy.controller;

import com.tzy.pojo.Result;
import com.tzy.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    /**
     * AI聊天接口
     * @param message 用户消息
     * @param type AI类型 (1-3)
     * @return 聊天响应
     */
    @PostMapping
    public Result chat(@RequestParam String message, @RequestParam Integer type) {
        // 参数校验
        if (message == null || message.trim().isEmpty()) {
            return Result.error("消息不能为空");
        }
        if (type == null || type < 1 || type > 3) {
            return Result.error("无效的AI类型");
        }

        try {
            String response = chatService.chat(message, type);
            return Result.success(response);
        } catch (HttpClientErrorException.Unauthorized e) {
            return Result.error("API密钥无效或已过期");
        } catch (HttpClientErrorException.TooManyRequests e) {
            return Result.error("请求过于频繁，请稍后再试");
        } catch (Exception e) {
            return Result.error("聊天失败: " + e.getMessage());
        }
    }

    /**
     * 获取AI类型列表
     */
    @GetMapping("/types")
    public Result getAITypes() {
        return Result.success(chatService.getAITypes());
    }
} 