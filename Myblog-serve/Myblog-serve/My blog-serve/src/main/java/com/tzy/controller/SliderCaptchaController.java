package com.tzy.controller;


import com.tzy.dto.CaptchaVerifyRequest;
import com.tzy.dto.ResponseResult;
import com.tzy.service.CaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 滑动验证码控制器
 * 处理验证码的生成和验证
 */
@RestController
@RequestMapping("/captcha")
public class SliderCaptchaController {

    @Autowired
    private CaptchaService captchaService;

    /**
     * 获取验证码
     * 生成背景图和滑块图，并返回给前端
     *
     * @return 验证码信息
     */
    @GetMapping("/get")
    public ResponseResult getCaptcha() {
        try {
            // 调用服务层生成验证码
            return captchaService.generateCaptcha();
        } catch (Exception e) {
            return ResponseResult.error("获取验证码失败: " + e.getMessage());
        }
    }

    /**
     * 验证滑动结果
     * 验证用户滑动的位置是否正确
     *
     * @param request 包含验证码ID和滑动位置
     * @return 验证结果
     */
    @PostMapping("/verify")
    public ResponseResult verifyCaptcha(@RequestBody CaptchaVerifyRequest request) {
        try {
            // 调用服务层验证滑动结果
            return captchaService.verifyCaptcha(request.getCaptchaId(), request.getX());
        } catch (Exception e) {
            return ResponseResult.error("验证失败: " + e.getMessage());
        }
    }
}
