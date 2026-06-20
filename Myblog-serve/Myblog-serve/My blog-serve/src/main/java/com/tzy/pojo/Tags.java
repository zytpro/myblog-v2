package com.tzy.pojo;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tags")
public class Tags {

    private Integer id;
    private String name;
    private String status;
}
