package com.tzy.pojo;

import lombok.Data;

@Data
public class SliderCaptchaVO {
    private String captchaId;        // 验证码ID
    private String backgroundImage;   // 背景图片（Base64）
    private String sliderImage;      // 滑块图片（Base64）
    private Integer width;           // 图片宽度
    private Integer height;          // 图片高度
} 