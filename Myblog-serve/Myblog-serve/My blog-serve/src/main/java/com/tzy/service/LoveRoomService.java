package com.tzy.service;

import com.tzy.pojo.LoveRoom;
import org.springframework.web.multipart.MultipartFile;

/**
 * 恋爱小屋服务接口
 *
 * @author tzy
 * @date 2024/04/04
 */
public interface LoveRoomService {

    /**
     * 获取恋爱小屋信息
     *
     * @return 恋爱小屋信息
     */
    LoveRoom getLoveRoomInfo();

    /**
     * 更新恋爱小屋信息
     *
     * @param loveRoom 恋爱小屋信息
     * @return 更新是否成功
     */
    boolean updateLoveRoom(LoveRoom loveRoom);

    /**
     * 上传头像
     *
     * @param file 头像文件
     * @param avatarType 头像类型（boy/girl）
     * @return 头像访问URL
     * @throws Exception 上传异常
     */
    String uploadAvatar(MultipartFile file, String avatarType) throws Exception;  // avatarType: "boy" 或 "girl"
}
