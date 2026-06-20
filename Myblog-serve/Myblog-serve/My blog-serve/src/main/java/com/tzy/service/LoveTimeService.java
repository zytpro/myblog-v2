package com.tzy.service;

import com.tzy.pojo.LoveTime;

import java.util.List;

/**
 * 恋爱时光服务接口
 *
 * @author tzy
 * @date 2024/04/05
 */
public interface LoveTimeService {

    /**
     * 添加一条恋爱时光记录
     *
     * @param loveTime 恋爱时光记录
     * @return 是否添加成功
     */
    boolean addLoveTime(LoveTime loveTime);

    /**
     * 获取所有恋爱时光记录
     *
     * @return 恋爱时光记录列表
     */
    List<LoveTime> getAllLoveTime();

    /**
     * 获取指定发起人的恋爱时光记录
     *
     * @param creatPeople 发起人（1:男生 0：女生）
     * @return 恋爱时光记录列表
     */
    List<LoveTime> getLoveTimeByCreatPeople(String creatPeople);

    /**
     * 根据ID删除记录
     */
    boolean deleteLoveTime(String id);
}
