package com.tzy.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class Visitor {
    private Integer id;
    @TableField("ip_address")
    private String ipAddress;
    @TableField("visit_date")
    private String visitDate;
}
