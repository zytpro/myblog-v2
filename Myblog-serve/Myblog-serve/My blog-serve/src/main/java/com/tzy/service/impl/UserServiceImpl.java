package com.tzy.service.impl;

import com.tzy.mapper.UserMapper;
import com.tzy.pojo.User;
import com.tzy.service.UserService;
import com.tzy.utils.Md5Util;
import com.tzy.utils.ROLE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Override
    public User findByUsername(String username) {
        User user = userMapper.findByUsername(username);
        return user;
    }

    @Override
    public void register(String username, String password) {
        //加密密码
        String md5Password = Md5Util.getMD5String(password);
        //默认角色组为普通用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(md5Password);
        user.setEmail(null);
        user.setRule("1");
        //添加
        userMapper.add(user);

    }

    @Override
    public void update(User u) {
        //构建更新对象
        User user = new User();
        user.setId(u.getId());
        user.setRule(u.getRule());
        user.setPassword(u.getPassword());
        user.setEmail(u.getEmail()!=null?u.getEmail():null);
        user.setUsername(u.getUsername());
        userMapper.update(user);

    }

    @Override
    public User getPersonalInfo() {
        User user = userMapper.getPersonalInfo();
        user.setPassword(null);
        return user;
    }

    @Override
    public void updatePersonalInfo(User user) {
       userMapper.updatePersonalInfo(user);

    }
}
