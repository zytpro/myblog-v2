package com.tzy.service.impl;

import com.tzy.pojo.LoveRoom;
import com.tzy.mapper.LoveRoomMapper;
import com.tzy.service.FileUploadService;
import com.tzy.service.LoveRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * 恋爱小屋服务实现类
 *
 * @author tzy
 * @date 2024/04/04
 */
@Service
public class LoveRoomServiceImpl implements LoveRoomService {

    /**
     * 恋爱小屋数据访问接口
     */
    @Autowired
    private LoveRoomMapper loveRoomMapper;

    /**
     * 文件上传服务
     */
    @Autowired
    private FileUploadService fileUploadService;

    /**
     * HTTP请求对象
     */
    @Autowired
    private HttpServletRequest request;

    /**
     * 获取恋爱小屋信息
     *
     * @return 恋爱小屋信息
     */
    @Override
    public LoveRoom getLoveRoomInfo() {
        return loveRoomMapper.getLoveRoomInfo();
    }

    /**
     * 更新恋爱小屋信息
     * 如果数据不存在则插入，存在则更新
     *
     * @param loveRoom 恋爱小屋信息
     * @return 操作是否成功
     */
    @Override
    public boolean updateLoveRoom(LoveRoom loveRoom) {
        int count = loveRoomMapper.count();
        if (count == 0) {
            return loveRoomMapper.insertLoveRoom(loveRoom) > 0;
        } else {
            return loveRoomMapper.updateLoveRoom(loveRoom) > 0;
        }
    }

    /**
     * 上传头像并更新对应的头像URL
     *
     * @param file 头像文件
     * @param avatarType 头像类型（boy/girl）
     * @return 头像访问URL
     * @throws Exception 上传过程中的异常
     */
    @Override
    public String uploadAvatar(MultipartFile file, String avatarType) throws Exception {
        // 验证头像类型
        if (!"boy".equals(avatarType) && !"girl".equals(avatarType)) {
            throw new IllegalArgumentException("头像类型必须是 'boy' 或 'girl'");
        }

        // 使用 FileUploadService 上传文件到 loveroom 目录
        FileUploadService.UploadResult result = fileUploadService.uploadFile(file, "loveroom", null, request);

        // 获取当前的恋爱小屋信息
        LoveRoom loveRoom = getLoveRoomInfo();
        if (loveRoom == null) {
            loveRoom = new LoveRoom();
        }

        // 更新对应的头像URL
        if ("boy".equals(avatarType)) {
            loveRoom.setBoyAvatar(result.getUrl());
        } else {
            loveRoom.setGirlAvatar(result.getUrl());
        }

        // 保存更新
        updateLoveRoom(loveRoom);

        return result.getUrl();
    }
}
