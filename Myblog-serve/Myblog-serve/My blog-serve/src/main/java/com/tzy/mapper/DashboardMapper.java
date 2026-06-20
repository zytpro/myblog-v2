package com.tzy.mapper;

import com.tzy.pojo.CategoryCountDTO;
import com.tzy.pojo.Dashboard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DashboardMapper {

    @Select("SELECT * FROM dashboard LIMIT 1")
    Dashboard getDashboardData();

    @Select("SELECT category, COUNT(*) as count " +
        "FROM article_categories " +
        "WHERE status = 1 " +
        "GROUP BY category")
    List<CategoryCountDTO> getCategoryArticleCount();
}
