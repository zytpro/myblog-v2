package com.tzy.pojo;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class Message {
    private Integer id;
    private String message;
    //定义时间格式
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String createTime;
    private String color;
    private Integer act;

}
