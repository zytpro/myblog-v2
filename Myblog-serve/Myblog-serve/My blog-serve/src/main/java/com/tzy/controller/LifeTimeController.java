package com.tzy.controller;

import com.tzy.pojo.LifeTime;
import com.tzy.pojo.Result;
import com.tzy.service.LifeTimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 生活时光控制器
 * 
 * @author tzy
 * @date 2024/04/05
 */
@RestController
@RequestMapping("/life-time")
public class LifeTimeController {

    @Autowired
    private LifeTimeService lifeTimeService;

    /**
     * 添加生活时光记录
     * 
     * @param file 照片文件
     * @param content 照片描述
     * @return 添加结果
     */
    @PostMapping
    public Result<String> addLifeTime(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "content", required = false) String content) {
        try {
            boolean success = lifeTimeService.addLifeTime(file, content);
            return success ? Result.success("添加成功") : Result.error("添加失败");
        } catch (Exception e) {
            return Result.error("添加生活时光记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有生活时光记录
     * 
     * @return 生活时光记录列表
     */
    @GetMapping
    public Result<List<LifeTime>> getAllLifeTime() {
        try {
            List<LifeTime> lifeTimeList = lifeTimeService.getAllLifeTime();
            return Result.success(lifeTimeList);
        } catch (Exception e) {
            return Result.error("获取生活时光记录失败: " + e.getMessage());
        }
    }

    /**
     * 删除生活时光记录
     * 
     * @param url 照片链接
     * @return 删除结果
     */
    @DeleteMapping
    public Result<String> deleteLifeTime(@RequestParam String url) {
        try {
            boolean success = lifeTimeService.deleteLifeTime(url);
            return success ? Result.success("删除成功") : Result.error("删除失败");
        } catch (Exception e) {
            return Result.error("删除生活时光记录失败: " + e.getMessage());
        }
    }

    /**
     * 更新生活时光记录
     * 
     * @param lifeTime 生活时光记录
     * @return 更新结果
     */
    @PutMapping
    public Result<String> updateLifeTime(@RequestBody LifeTime lifeTime) {
        try {
            boolean success = lifeTimeService.updateLifeTime(lifeTime);
            return success ? Result.success("更新成功") : Result.error("更新失败");
        } catch (Exception e) {
            return Result.error("更新生活时光记录失败: " + e.getMessage());
        }
    }
} 