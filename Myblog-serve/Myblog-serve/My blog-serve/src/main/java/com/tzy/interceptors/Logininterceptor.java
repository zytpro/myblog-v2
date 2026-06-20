package com.tzy.interceptors;

import com.tzy.annotations.NeedLogin;
import com.tzy.utils.JwtUtil;
import com.tzy.utils.ThreadLocalUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Map;

@Component
public class Logininterceptor implements HandlerInterceptor {

    private final StringRedisTemplate stringRedisTemplate;

    public Logininterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 仅对标注了 @NeedLogin 的方法进行拦截
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            NeedLogin needLogin = method.getAnnotation(NeedLogin.class);

            // 如果没有 @NeedLogin 注解，直接放行
            if (needLogin == null) {
                return true;
            }
        }

        // 令牌验证
        String token = request.getHeader("Authorization");

        try {
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            String redisToken = operations.get(token);
            if (redisToken == null) {
                throw new Exception();
            }

            Map<String, Object> claims = JwtUtil.parseToken(token);
            // 把数据存储到 ThreadLocal 中
            ThreadLocalUtil.set(claims);
            // 验证成功
            return true;
        } catch (Exception e) {
            // HTTP 状态码为 401
            response.setStatus(401);
            // 不通过验证
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清除 ThreadLocal 中的数据
        ThreadLocalUtil.remove();
    }
}
