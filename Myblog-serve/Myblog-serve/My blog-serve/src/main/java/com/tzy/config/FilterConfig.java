package com.tzy.config;

import com.tzy.filter.RequestLoggingFilter;
import com.tzy.filter.VisitorFilter;
import com.tzy.service.VisitorService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<RequestLoggingFilter> loggingFilter() {
        FilterRegistrationBean<RequestLoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestLoggingFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public VisitorFilter visitorFilter(VisitorService visitorService) {
        return new VisitorFilter(visitorService);
    }

    @Bean
    public FilterRegistrationBean<VisitorFilter> visitorFilterRegistration(VisitorFilter visitorFilter) {
        FilterRegistrationBean<VisitorFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(visitorFilter);
        registration.addUrlPatterns("/*");
        registration.setOrder(2);
        return registration;
    }
}

