package com.tzy.controller;

import com.tzy.pojo.CategoryCountDTO;
import com.tzy.pojo.Dashboard;
import com.tzy.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping
    public Dashboard getDashboardData() {
        return dashboardService.getDashboardData();
    }

    // 获取每个分类的文章数量
    @GetMapping("/articleCategoryCount")
    public List<CategoryCountDTO> getArticleCategoryCount() {
        return dashboardService.getCategoryArticleCount();
    }

}
