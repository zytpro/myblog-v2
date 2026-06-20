package com.tzy.controller;

import com.tzy.pojo.LoveRoom;
import com.tzy.pojo.Result;
import com.tzy.service.LoveRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 恋爱小屋控制器
 *
 * @author tzy
 * @date 2024/04/04
 */
@RestController
@RequestMapping("/love-room")
public class LoveRoomController {

    /**
     * 恋爱小屋服务
     */
    @Autowired
    private LoveRoomService loveRoomService;

    /**
     * 获取恋爱小屋信息
     *
     * @return 恋爱小屋信息
     */
    @GetMapping
    public Result<LoveRoom> getLoveRoomInfo() {
        try {
            LoveRoom loveRoom = loveRoomService.getLoveRoomInfo();
            return Result.success(loveRoom);
        } catch (Exception e) {
            return Result.error("获取恋爱小屋信息失败: " + e.getMessage());
        }
    }

    /**
     * 更新恋爱小屋信息
     *
     * @param loveRoom 恋爱小屋信息
     * @return 更新结果
     */
    @PostMapping
    public Result<String> updateLoveRoom(@RequestBody LoveRoom loveRoom) {
        try {
            boolean success = loveRoomService.updateLoveRoom(loveRoom);
            return success ? Result.success("更新成功") : Result.error("更新失败");
        } catch (Exception e) {
            return Result.error("更新恋爱小屋信息失败: " + e.getMessage());
        }
    }

    /**
     * 上传头像
     *
     * @param file 头像文件
     * @param avatarType 头像类型（boy/girl）
     * @return 头像访问URL
     */
    @PostMapping("/upload/avatar/{avatarType}")
    public Result<String> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @PathVariable("avatarType") String avatarType) {
        try {
            if (!"boy".equals(avatarType) && !"girl".equals(avatarType)) {
                return Result.error("头像类型必须是 'boy' 或 'girl'");
            }
            String fileUrl = loveRoomService.uploadAvatar(file, avatarType);
            return Result.success(fileUrl);
        } catch (Exception e) {
            return Result.error("上传头像失败: " + e.getMessage());
        }
    }
}
