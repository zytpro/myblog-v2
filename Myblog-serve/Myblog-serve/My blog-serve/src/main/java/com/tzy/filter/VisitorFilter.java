package com.tzy.filter;

import com.tzy.service.VisitorService;
// import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

// 移除这行
// @Component
public class VisitorFilter implements Filter {
    
    private final VisitorService visitorService;

    public VisitorFilter(VisitorService visitorService) {
        this.visitorService = visitorService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI();

        // 只处理非静态资源且非访客统计接口的请求
        if (!path.matches(".*(css|js|png|jpg|gif|ico|woff|ttf)$") 
            && !path.startsWith("/visitor")) {
            visitorService.updateUniqueVisitors(httpRequest);
        }

        chain.doFilter(request, response);
    }
}
