package com.tzy.service;

import com.tzy.pojo.User;

public interface UserService {
    // 根据用户名查询用户信息
    public User findByUsername(String username);

    // 注册新用户
    void register(String username, String password);

    // 更新用户信息
    void update(User u);

    // 获取用户个人信息
    User getPersonalInfo();

    // 更新用户个人信息
    void updatePersonalInfo(User user);
}
