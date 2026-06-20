package com.tzy.pojo;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

/**
 * 恋爱时光实体类
 * @author tzy
 */
@Data
public class LoveTime {
    /**
     * 主键ID
     */
    private String id;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date creatTime;
    
    /**
     * 对话内容
     */
    private String content;
    
    /**
     * 发起人 1:男生 0:女生
     */
    private String creatPeople;
}
