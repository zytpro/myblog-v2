package com.tzy.service.impl;


import com.tzy.dto.ResponseResult;
import com.tzy.pojo.CaptchaInfo;
import com.tzy.service.CaptchaService;
import com.tzy.utils.CaptchaUtils;


import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 验证码服务实现类
 */
@Service
public class CaptchaServiceImpl implements CaptchaService {

    /**
     * 验证码信息存储
     * 实际生产环境建议使用Redis等缓存存储
     */
    private static final Map<String, CaptchaInfo> CAPTCHA_CACHE = new ConcurrentHashMap<>();

    /**
     * 验证码有效期（分钟）
     */
    private static final int CAPTCHA_EXPIRE_MINUTES = 5;

    /**
     * 滑动验证的误差范围（像素）
     */
    private static final int TOLERANCE = 5;

    @Override
    public ResponseResult generateCaptcha() throws Exception {
        // 生成验证码唯一标识
        String captchaId = UUID.randomUUID().toString();

        // 生成背景图和滑块图
        int blockSize = 40; // 滑块大小
        int width = 300;    // 背景图宽度
        int height = 150;   // 背景图高度

        // 随机生成滑块位置
        int blockX = CaptchaUtils.getRandomInt(width / 3, width - blockSize - 10);
        int blockY = CaptchaUtils.getRandomInt(10, height - blockSize - 10);

        // 生成背景图和滑块图
        BufferedImage[] images = CaptchaUtils.createCaptchaImages(width, height, blockX, blockY, blockSize);
        BufferedImage bgImage = images[0];
        BufferedImage blockImage = images[1];

        // 将图片转为Base64编码
        String bgImageBase64 = CaptchaUtils.imageToBase64(bgImage);
        String blockImageBase64 = CaptchaUtils.imageToBase64(blockImage);

        // 创建验证码信息对象
        CaptchaInfo captchaInfo = new CaptchaInfo();
        captchaInfo.setCaptchaId(captchaId);
        captchaInfo.setBlockX(blockX);
        captchaInfo.setBlockY(blockY);
        captchaInfo.setCreateTime(LocalDateTime.now());

        // 存储验证码信息
        CAPTCHA_CACHE.put(captchaId, captchaInfo);

        // 构建返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("captchaId", captchaId);
        data.put("bgImage", "data:image/png;base64," + bgImageBase64);
        data.put("blockImage", "data:image/png;base64," + blockImageBase64);
        data.put("y", blockY);

        return ResponseResult.success("获取验证码成功", data);
    }

    @Override
    public ResponseResult verifyCaptcha(String captchaId, int x) throws Exception {
        // 获取验证码信息
        CaptchaInfo captchaInfo = CAPTCHA_CACHE.get(captchaId);

        // 验证码不存在
        if (captchaInfo == null) {
            return ResponseResult.error("验证码不存在或已过期");
        }

        // 验证码已使用
        if (captchaInfo.isUsed()) {
            return ResponseResult.error("验证码已使用");
        }

        // 验证码已过期
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(captchaInfo.getCreateTime(), now);
        if (minutes > CAPTCHA_EXPIRE_MINUTES) {
            CAPTCHA_CACHE.remove(captchaId);
            return ResponseResult.error("验证码已过期");
        }

        // 验证滑动位置
        int blockX = captchaInfo.getBlockX();
        if (Math.abs(x - blockX) <= TOLERANCE) {
            // 验证通过，生成验证token
            String token = UUID.randomUUID().toString();

            // 标记验证码已使用
            captchaInfo.setUsed(true);

            // 构建返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);

            return ResponseResult.success("验证通过", data);
        } else {
            return ResponseResult.error("验证失败，请重试");
        }
    }
}
