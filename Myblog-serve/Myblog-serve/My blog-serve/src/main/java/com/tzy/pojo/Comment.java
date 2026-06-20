package com.tzy.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Comment {
    private Long id;
    private Long articleId;
    private Long parentId;
    private String content;
    private String nickname;
    private String email;
    private String ipAddress;
    private String userAgent;
    private Integer status;
    private LocalDateTime createdAt;


}

