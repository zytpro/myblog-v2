package com.tzy.controller;

import com.tzy.pojo.Message;
import com.tzy.pojo.Result;
import com.tzy.service.MessageService;
import com.tzy.service.impl.RateLimiterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private RateLimiterService rateLimiterService;
    /**
     * 获取弹幕接口
     */
    @GetMapping
    public Result<List<Message>> getAllMessages() {
        List<Message> messages = messageService.getAllMessages();
        if(messages == null || messages.isEmpty()) {
            return Result.error("获取失败");
        }
        return Result.success(messages);
    }

    /**
     * 添加弹幕接口
     */
    @PostMapping("/add")
    public Result addMessage(@RequestBody Message message, HttpServletRequest request) {
        String ip = getClientIP(request); // 获取客户端 IP 地址
        boolean allowed = rateLimiterService.isAllowed(ip);
        if (!allowed) {
            System.out.println("IP: " + ip + " 发送弹幕过于频繁，请稍后再试");
            return Result.error("一天只能发送 5 条弹幕，请稍后再试");
        }

        // 处理添加弹幕的逻辑
        boolean result = messageService.addMessage(message.getMessage());
        if (result) {
            return Result.success();
        } else {
            return Result.error("添加失败");
        }
    }


    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr(); // 如果没有 X-Forwarded-For 头部，则使用 getRemoteAddr 获取 IP
        }
        return ip;
    }

    /**
     * 删除弹幕接口
     */
//    @PostMapping("/delete")
//    public Result deleteMessage(String message){
//        Boolean result = messageService.deleteMessage(message);
//        if(result){
//            return Result.success();
//        }else{
//            return Result.error("删除失败");
//        }
}
