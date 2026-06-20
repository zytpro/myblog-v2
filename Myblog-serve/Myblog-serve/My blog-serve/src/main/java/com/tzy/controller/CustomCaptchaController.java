package com.tzy.controller;

import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/custom-captcha")
public class CustomCaptchaController {

    @Autowired
    private CaptchaService captchaService;

    /**
     * 获取验证码
     */
    @PostMapping("/get")
    public ResponseModel get(@RequestBody CaptchaVO captchaVO) {
        return captchaService.get(captchaVO);
    }

    /**
     * 校验验证码
     */
    @PostMapping("/check")
    public ResponseModel check(@RequestBody CaptchaVO captchaVO) {
        return captchaService.check(captchaVO);
    }

    /**
     * 二次校验验证码
     */
    @PostMapping("/verification")
    public ResponseModel verification(@RequestBody CaptchaVO captchaVO) {
        return captchaService.verification(captchaVO);
    }
}