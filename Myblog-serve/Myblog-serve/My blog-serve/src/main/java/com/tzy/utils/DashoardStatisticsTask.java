package com.tzy.utils;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@Component
public class DashoardStatisticsTask {

    @Autowired
    private JdbcTemplate jdbcTemplate;

//    @Scheduled(cron = "0 0 * * * ?") // 每 1 小时执行一次
    @Scheduled(cron = "*/5 * * * * ?") // 每 5 秒执行一次
    public void updateDashboardStats() {
        // 查询访问人数
        String visitorCountSql = "SELECT COUNT(DISTINCT ip_address, visit_date) FROM visitor";
        Integer visitorCount = jdbcTemplate.queryForObject(visitorCountSql, Integer.class);

        // 查询文章数量
        String articleCountSql = "SELECT COUNT(*) FROM article";
        Integer articleCount = jdbcTemplate.queryForObject(articleCountSql, Integer.class);

        // 查询评论数量
        String commentCountSql = "SELECT COUNT(*) FROM comment WHERE status = 1"; // 统计正常状态的评论
        Integer commentCount = jdbcTemplate.queryForObject(commentCountSql, Integer.class);

        // 更新 dashboard 表
        String updateSql = "UPDATE dashboard SET visitor = ?, article_number = ?, comment_number = ?";
        jdbcTemplate.update(updateSql, visitorCount, articleCount, commentCount);

//        System.out.println("已更新仪表盘：访问人数 = " + visitorCount + "，文章数量 = " + articleCount + "，评论数量 = " + commentCount);
//
    }
}
