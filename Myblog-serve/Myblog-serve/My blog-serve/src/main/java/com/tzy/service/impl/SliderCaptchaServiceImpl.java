package com.tzy.service.impl;

import com.tzy.pojo.SliderCaptchaVO;
import com.tzy.service.SliderCaptchaService;
import com.tzy.utils.CaptchaUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class SliderCaptchaServiceImpl implements SliderCaptchaService {

    private static final String CAPTCHA_KEY_PREFIX = "slider_captcha:";
    private static final int EXPIRE_MINUTES = 5;
    private static final int VALID_OFFSET = 5;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public SliderCaptchaVO generateCaptcha() {
        try {
            // 设置验证码图片尺寸
            int width = 300;
            int height = 150;
            
            // 生成背景图和滑块图
            BufferedImage backgroundImage = CaptchaUtils.createBackgroundImage(width, height);
            int randomX = CaptchaUtils.generateRandomXPosition();
            BufferedImage sliderImage = CaptchaUtils.createSliderImage(backgroundImage, randomX);

            // 生成验证码ID
            String captchaId = UUID.randomUUID().toString();

            // 存储验证信息到Redis
            redisTemplate.opsForValue().set(
                    CAPTCHA_KEY_PREFIX + captchaId,
                    randomX,
                    EXPIRE_MINUTES,
                    TimeUnit.MINUTES
            );

            // 构建返回对象
            SliderCaptchaVO captchaVO = new SliderCaptchaVO();
            captchaVO.setCaptchaId(captchaId);
            captchaVO.setBackgroundImage(CaptchaUtils.imageToBase64(backgroundImage));
            captchaVO.setSliderImage(CaptchaUtils.imageToBase64(sliderImage));
            captchaVO.setWidth(backgroundImage.getWidth());
            captchaVO.setHeight(backgroundImage.getHeight());

            return captchaVO;
        } catch (IOException e) {
            throw new RuntimeException("生成验证码图片失败", e);
        }
    }

    @Override
    public boolean verifyCaptcha(String captchaId, Integer xPosition) {
        String key = CAPTCHA_KEY_PREFIX + captchaId;
        Object correctPosition = redisTemplate.opsForValue().get(key);

        if (correctPosition == null) {
            return false;
        }

        // 验证完成后删除验证码
        redisTemplate.delete(key);

        // 验证位置是否在允许的误差范围内
        int correct = (int) correctPosition;
        return Math.abs(xPosition - correct) <= VALID_OFFSET;
    }
} 