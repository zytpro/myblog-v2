package com.tzy.service;

import com.tzy.pojo.SliderCaptchaVO;

public interface SliderCaptchaService {
    /**
     * 生成滑动验证码
     */
    SliderCaptchaVO generateCaptcha();

    /**
     * 验证滑动验证码
     */
    boolean verifyCaptcha(String captchaId, Integer xPosition);
} 