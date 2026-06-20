package com.tzy.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class Announcement {
    private Integer id;
    private String content;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createTime;
    private Integer status;
}

