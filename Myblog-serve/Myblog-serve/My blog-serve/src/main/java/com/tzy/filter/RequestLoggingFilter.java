package com.tzy.filter;

import com.tzy.config.Logger;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RequestLoggingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestPath = httpRequest.getRequestURI();
        
        // 跳过静态资源的日志记录
        if (requestPath.matches(".*(css|js|png|jpg|gif|ico|woff|ttf)$")) {
            chain.doFilter(request, response);
            return;
        }

        long startTime = System.currentTimeMillis();
        try {
            Logger.info(String.format("[REQUEST] %s %s", httpRequest.getMethod(), requestPath));
            chain.doFilter(request, response);
            Logger.info(String.format("[RESPONSE] %s %s - %dms - status: %d", 
                httpRequest.getMethod(), 
                requestPath,
                System.currentTimeMillis() - startTime,
                httpResponse.getStatus()));
        } catch (Exception e) {
            Logger.error(String.format("[ERROR] %s %s - %s", 
                httpRequest.getMethod(), requestPath, e.getMessage()));
            throw e;
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {
        Logger.info("RequestLoggingFilter initialized");
    }

    @Override
    public void destroy() {
        Logger.info("RequestLoggingFilter destroyed");
    }
}
