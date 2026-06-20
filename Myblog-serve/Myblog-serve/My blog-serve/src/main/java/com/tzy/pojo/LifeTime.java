package com.tzy.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 生活时光实体类
 *
 * @author tzy
 * @date 2024/04/05
 */
@Data
public class LifeTime {

    /**
     * 照片描述
     */
    private String content;

    /**
     * 照片链接
     */
    private String url;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date createTime;
}
