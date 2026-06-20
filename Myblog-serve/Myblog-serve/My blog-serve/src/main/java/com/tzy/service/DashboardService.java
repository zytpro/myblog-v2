package com.tzy.service;

import com.tzy.pojo.CategoryCountDTO;
import com.tzy.pojo.Dashboard;

import java.util.List;

public interface DashboardService {
    Dashboard getDashboardData();

    List<CategoryCountDTO> getCategoryArticleCount();
}
