package com.tzy.pojo;

import lombok.Data;

@Data
public class Navbar {
    private String name;    // 导航栏名字
    private String state;   // 状态，1：可用，0：不可用
} 