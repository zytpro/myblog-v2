package com.tzy.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.tzy.pojo.Result;
import com.tzy.pojo.User;
import com.tzy.service.UserService;
import com.tzy.utils.Md5Util;
import com.tzy.utils.ThreadLocalUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserService userService;
   //注册接口
    @PostMapping("/register")
    public Result register(String username, String password) {
        //查询数据库中是否有相同用户名
        User u =userService.findByUsername(username);
        if (u != null) {
            return Result.error("用户名已存在");

        }
        //注册
        userService.register(username, password);
        return Result.success("注册成功");
    }

    //登录接口
    @PostMapping("/login")
    public SaResult  login(@RequestBody User user) {
         String username = user.getUsername();
         String password = user.getPassword();
        //查询数据库中用户是否存在
        User u = userService.findByUsername(username);
        if (u == null){
            return SaResult.error("用户名不存在");
        }
        //验证密码
        String md5Password = Md5Util.getMD5String(password);
        if(u.getPassword().equals(md5Password)){
            //登录成功,生成token
            StpUtil.logout();
            // 使用默认超时时间登录（2小时绝对过期，30分钟活跃超时）
            StpUtil.login(u.getId());
            SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
            
            // 添加用户信息到响应中
            u.setPassword(null); // 不返回密码
            
            // 将用户信息转换为JSON字符串存储到token的tag中
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String userJson = objectMapper.writeValueAsString(u);
                tokenInfo.setTag(userJson);
            } catch (Exception e) {
                // 如果JSON转换失败，只存储用户ID
                tokenInfo.setTag(String.valueOf(u.getId()));
            }
            
            return SaResult.data(tokenInfo);
        }
        return SaResult.error("账号或密码错误");
    }

    //修改密码接口
    @PostMapping("/updatePassword")
    public Result updatePassword(String username, String oldPassword,String newPassword){
        //查询数据库中用户是否存在
        User u = userService.findByUsername(username);
        if(u == null){
            return Result.error("异常错误，请联系管理员");
        }
        //验证密码
        String md5Password = Md5Util.getMD5String(oldPassword);
        if(!md5Password.equals(u.getPassword())){
            return Result.error("原密码错误");
        }
        //修改密码
        String md5NewPassword = Md5Util.getMD5String(newPassword);
        //清除令牌
        StpUtil.logout();
        u.setPassword(md5NewPassword);
        userService.update(u);
        return Result.success("密码修改成功");
    }

    //修改用户名
    @PostMapping("/updateUsername")
    public Result updateUsername(String oldUsername, String newUsername){
        //查询数据库中用户是否存在
        User u = userService.findByUsername(oldUsername);
        if(u == null){
            return Result.error("未知错误，请联系管理员");
        }
        //修改用户名
        u.setUsername(newUsername);
        userService.update(u);
        return Result.success("用户名修改成功");
    }

    //获取用户信息
    @GetMapping("/userInfo")
    public Result<User> userInfo(){
        Map<String, Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");

        User user = userService.findByUsername(username);
        return Result.success(user);

    }

    //刷新token接口
    @PostMapping("/refreshToken")
    public SaResult refreshToken(){
        // 检查当前token是否有效
        if (!StpUtil.isLogin()) {
            return SaResult.error("token无效或已过期");
        }
        
        // 刷新token活跃时间（重新设置活跃超时时间）
        StpUtil.renewTimeout(1800); // 重新设置30分钟活跃超时
        
        // 获取新的token信息
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        
        return SaResult.data(tokenInfo);
    }

    //检查token状态接口
    @PostMapping("/checkToken")
    public SaResult checkToken(){
        if (StpUtil.isLogin()) {
            SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
            return SaResult.data(tokenInfo);
        } else {
            return SaResult.error("token无效或已过期");
        }
    }

    //获取个人信息(只获取数据库里面的管理员数据)
    @PostMapping("/getPersonalInfo")
    public Result<User> getPersonalInfo(){
       User user = userService.getPersonalInfo();
        return Result.success(user);
    }

    //更新个人信息
    @PostMapping("/updatePersonalInfo")
    public Result updatePersonalInfo(@RequestBody User user){
        userService.updatePersonalInfo(user);
        return Result.success("个人信息更新成功");
    }


    }

