package com.tzy.mapper;

import com.tzy.pojo.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    // 根据用户名查询用户信息
    @Select("select * from users where username = #{username}")
    User findByUsername(@Param("username") String username);

    // 添加用户
    @Insert("insert into users(username, password, email,rule) values(#{username}, #{password}, #{email},#{rule})")
    void add(User u);

    // 更新用户信息
    @Update("update users set password = #{password}, username = #{username}, email = #{email} where id = #{id}\n")
    void update(User u);

    @Select("SELECT * FROM users LIMIT 1")
    User getPersonalInfo();

    // 更新个人信息
    @Update("UPDATE users set username = #{user.username}, users.avatar = #{user.avatar}, email = #{user.email},phone = #{user.phone},description = #{user.description},rule = #{user.rule} where id = #{user.id}")
    void updatePersonalInfo(@Param("user") User user);
}
