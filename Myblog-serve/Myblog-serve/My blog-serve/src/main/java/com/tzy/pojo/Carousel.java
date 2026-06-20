package com.tzy.pojo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Carousel {
    private Integer id;
    private String title;
    private String imageURL;
    private Integer articleId;
    private Integer status;
    private LocalDate createTime;
}
