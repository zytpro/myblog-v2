package com.tzy.pojo;

import java.time.LocalDateTime;

/**
 * 验证码信息实体类
 * 存储验证码的相关信息
 */
public class CaptchaInfo {

    /**
     * 验证码唯一标识
     */
    private String captchaId;

    /**
     * 滑块X坐标（正确位置）
     */
    private int blockX;

    /**
     * 滑块Y坐标
     */
    private int blockY;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 是否已使用
     */
    private boolean used;

    public CaptchaInfo() {
        this.createTime = LocalDateTime.now();
        this.used = false;
    }

    public String getCaptchaId() {
        return captchaId;
    }

    public void setCaptchaId(String captchaId) {
        this.captchaId = captchaId;
    }

    public int getBlockX() {
        return blockX;
    }

    public void setBlockX(int blockX) {
        this.blockX = blockX;
    }

    public int getBlockY() {
        return blockY;
    }

    public void setBlockY(int blockY) {
        this.blockY = blockY;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}
