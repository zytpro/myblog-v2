package com.tzy.service.impl;

import com.tzy.mapper.DashboardMapper;
import com.tzy.pojo.CategoryCountDTO;
import com.tzy.pojo.Dashboard;
import com.tzy.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private DashboardMapper dashboardMapper;

    @Override
    public Dashboard getDashboardData() {
        return dashboardMapper.getDashboardData();
    }

    // 获取每个分类的文章数量
    @Override
    public List<CategoryCountDTO> getCategoryArticleCount() {
        return dashboardMapper.getCategoryArticleCount();
    }
}
