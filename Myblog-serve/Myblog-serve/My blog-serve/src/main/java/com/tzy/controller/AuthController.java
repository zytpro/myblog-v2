package com.tzy.controller;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthController {

    @GetMapping("/validate")
    public Map<String, Object> validateToken(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 如果没有提供Authorization头，直接返回无效
            if (authorizationHeader == null || authorizationHeader.isEmpty()) {
                response.put("isValid", false);
                response.put("message", "No authorization header provided");
                return response;
            }

            // 提取 token（假设传递的格式是 "Bearer token" 或直接是 token）
            String token = authorizationHeader.replace("Bearer ", "").trim();

            // 使用 token 验证登录状态
            Object loginId = StpUtil.getLoginIdByToken(token); // 通过 token 获取登录 ID

            if (loginId != null) {
                response.put("isValid", true);
                response.put("message", "Token is valid");
                response.put("userId", loginId);
            } else {
                response.put("isValid", false);
                response.put("message", "Token is invalid or expired");
            }
        } catch (Exception e) {
            response.put("isValid", false);
            response.put("message", "Token is invalid or expired: " + e.getMessage());
        }

        return response;
    }
}


