package com.tzy.service;

import com.tzy.pojo.LifeTime;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 生活时光服务接口
 * 
 * @author tzy
 * @date 2024/04/05
 */
public interface LifeTimeService {
    
    /**
     * 添加生活时光记录
     * 
     * @param file 照片文件
     * @param content 照片描述
     * @return 是否添加成功
     */
    boolean addLifeTime(MultipartFile file, String content) throws Exception;
    
    /**
     * 获取所有生活时光记录
     * 
     * @return 生活时光记录列表
     */
    List<LifeTime> getAllLifeTime();
    
    /**
     * 删除生活时光记录
     * 
     * @param url 照片链接
     * @return 是否删除成功
     */
    boolean deleteLifeTime(String url);
    
    /**
     * 更新生活时光记录
     * 
     * @param lifeTime 生活时光记录
     * @return 是否更新成功
     */
    boolean updateLifeTime(LifeTime lifeTime);
} 