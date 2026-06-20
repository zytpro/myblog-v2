package com.tzy;



import com.tzy.start.CustomBanner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.Banner;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.mybatis.spring.annotation.MapperScan;

@EnableScheduling
@SpringBootApplication
@ServletComponentScan
@MapperScan("com.tzy.mapper")
public class MyBlogApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(MyBlogApplication.class);

        // 使用自定义的 Banner
        app.setBanner(new CustomBanner());

        app.setBannerMode(Banner.Mode.CONSOLE);
        app.run(args);
    }
}

