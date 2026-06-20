package com.tzy.service.impl;

import com.tzy.pojo.LoveTime;
import com.tzy.mapper.LoveTimeMapper;
import com.tzy.service.LoveTimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 恋爱时光服务实现类
 *
 * @author tzy
 * @date 2024/04/05
 */
@Service
public class LoveTimeServiceImpl implements LoveTimeService {

    @Autowired
    private LoveTimeMapper loveTimeMapper;

    @Override
    public boolean addLoveTime(LoveTime loveTime) {
        return loveTimeMapper.insert(loveTime) > 0;
    }

    @Override
    public List<LoveTime> getAllLoveTime() {
        return loveTimeMapper.selectAll();
    }

    @Override
    public List<LoveTime> getLoveTimeByCreatPeople(String creatPeople) {
        return loveTimeMapper.selectByCreatPeople(creatPeople);
    }

    @Override
    public boolean deleteLoveTime(String id) {
        return loveTimeMapper.deleteById(id) > 0;
    }
}
