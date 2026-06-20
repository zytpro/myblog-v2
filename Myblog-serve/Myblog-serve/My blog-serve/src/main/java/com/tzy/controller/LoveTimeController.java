package com.tzy.controller;

import com.tzy.pojo.LoveTime;
import com.tzy.pojo.Result;
import com.tzy.service.LoveTimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 恋爱时光控制器
 *
 * @author tzy
 * @date 2024/04/05
 */
@RestController
@RequestMapping("/love-time")
public class LoveTimeController {

    @Autowired
    private LoveTimeService loveTimeService;

    /**
     * 添加恋爱时光记录
     *
     * @param loveTime 恋爱时光记录
     * @return 添加结果
     */
    @PostMapping
    public Result<String> addLoveTime(@RequestBody LoveTime loveTime) {
        // 生成UUID作为ID
        loveTime.setId(UUID.randomUUID().toString());
        boolean success = loveTimeService.addLoveTime(loveTime);
        return success ? Result.success("添加成功") : Result.error("添加失败");
    }

    /**
     * 获取所有恋爱时光记录
     *
     * @return 恋爱时光记录列表
     */
    @GetMapping
    public Result<List<LoveTime>> getAllLoveTime() {
        List<LoveTime> list = loveTimeService.getAllLoveTime();
        return Result.success(list);
    }

    /**
     * 获取指定发起人的恋爱时光记录
     *
     * @param creatPeople 发起人（1:男生 0：女生）
     * @return 恋爱时光记录列表
     */
    @GetMapping("/{creatPeople}")
    public Result<List<LoveTime>> getLoveTimeByCreatPeople(@PathVariable String creatPeople) {
        if (!("0".equals(creatPeople) || "1".equals(creatPeople))) {
            return Result.error("发起人参数错误，应为0或1");
        }
        List<LoveTime> list = loveTimeService.getLoveTimeByCreatPeople(creatPeople);
        return Result.success(list);
    }

    /**
     * 删除指定时间的记录
     *
     * @param id 记录ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteLoveTime(@PathVariable String id) {
        boolean success = loveTimeService.deleteLoveTime(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
}
