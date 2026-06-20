package com.tzy.start;

import org.springframework.boot.Banner;
import org.springframework.core.env.Environment;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CustomBanner implements Banner {

    @Override
    public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
        // 获取当前时间
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        // 获取项目名称
        String appName = environment.getProperty("spring.application.name", "My Blog");
        // 获取启动端口，默认使用 8080
        String port = environment.getProperty("server.port", "8080");
        //作者信息
        String author = "Author: Tzy";
        out.println("**************************");
        out.println("   " + author);
        out.println("   " + appName);
        out.println("   Started at: " + currentTime);
        out.println("   Port: " + port);
        out.println("**************************");
    }
}
