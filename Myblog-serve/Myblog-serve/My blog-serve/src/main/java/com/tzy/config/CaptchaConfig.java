package com.tzy.config;

import com.anji.captcha.service.CaptchaCacheService;
import com.anji.captcha.service.impl.CaptchaServiceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CaptchaConfig {

    @Bean
    public CaptchaCacheService captchaCacheService() {
        return CaptchaServiceFactory.getCache("local");
    }
}
