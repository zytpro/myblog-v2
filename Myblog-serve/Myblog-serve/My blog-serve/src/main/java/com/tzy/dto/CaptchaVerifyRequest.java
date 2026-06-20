package com.tzy.dto;

/**
 * 验证码验证请求DTO
 * 用于接收前端验证请求
 */
public class CaptchaVerifyRequest {

    /**
     * 验证码ID
     */
    private String captchaId;

    /**
     * 用户滑动的X坐标
     */
    private int x;

    public String getCaptchaId() {
        return captchaId;
    }

    public void setCaptchaId(String captchaId) {
        this.captchaId = captchaId;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }
}
