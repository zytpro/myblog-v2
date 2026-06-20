package com.tzy.service;


import com.tzy.dto.ResponseResult;

/**
 * 验证码服务接口
 * 定义验证码相关的业务逻辑
 */
public interface CaptchaService {

    /**
     * 生成验证码
     *
     * @return 包含验证码信息的响应结果
     * @throws Exception 生成验证码过程中可能出现的异常
     */
    ResponseResult generateCaptcha() throws Exception;

    /**
     * 验证滑动结果
     *
     * @param captchaId 验证码ID
     * @param x 用户滑动的X坐标
     * @return 验证结果
     * @throws Exception 验证过程中可能出现的异常
     */
    ResponseResult verifyCaptcha(String captchaId, int x) throws Exception;
}
