package com.tzy.config;

import com.github.pagehelper.PageHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * PageHelper 配置类
 * 
 * @author tzy
 */
@Configuration
public class PageHelperConfig {

    @Bean
    public PageHelper pageHelper() {
        PageHelper pageHelper = new PageHelper();
        Properties properties = new Properties();
        
        // 设置数据库方言
        properties.setProperty("helperDialect", "mysql");
        // 设置分页合理化
        properties.setProperty("reasonable", "true");
        // 支持方法参数
        properties.setProperty("supportMethodsArguments", "true");
        // 参数映射
        properties.setProperty("params", "count=countSql");
        
        pageHelper.setProperties(properties);
        return pageHelper;
    }
}
