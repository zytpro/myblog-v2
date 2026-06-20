package com.tzy.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
public class RateLimiterService {

    private static final int MAX_REQUESTS_PER_DAY = 5;
    private static final long EXPIRATION_TIME = 86400L;  // 24小时 = 86400秒
    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 检查并更新用户 IP 的请求次数
     *
     * @param ip 客户端的 IP 地址
     * @return 是否允许请求
     */
    public boolean isAllowed(String ip) {
        String key = "ip:" + ip;
        String value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            // 如果该 IP 地址还没有请求记录，设置初始值
            redisTemplate.opsForValue().set(key, "1", Duration.ofSeconds(EXPIRATION_TIME));
            return true;
        }

        int currentCount = Integer.parseInt(value);
        if (currentCount < MAX_REQUESTS_PER_DAY) {
            // 如果请求次数未达到最大值，更新请求次数
            redisTemplate.opsForValue().increment(key, 1);
            return true;
        } else {
            // 超过最大请求次数
            return false;
        }
    }
}

