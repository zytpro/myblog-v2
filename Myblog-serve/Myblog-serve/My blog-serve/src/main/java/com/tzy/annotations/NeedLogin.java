package com.tzy.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，用于标记需要登录的接口
 */
@Target(ElementType.METHOD) // 该注解可以作用于方法
@Retention(RetentionPolicy.RUNTIME) // 运行时可用
public @interface NeedLogin {
}
