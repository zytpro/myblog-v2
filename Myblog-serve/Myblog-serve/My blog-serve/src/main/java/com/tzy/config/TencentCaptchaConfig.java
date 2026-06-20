package com.tzy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "tencent.captcha")
public class TencentCaptchaConfig {
    private String secretId;
    private String secretKey;
    private String captchaAppId;  // 验证码应用ID
    private String appSecretKey;  // 验证码密钥
} 