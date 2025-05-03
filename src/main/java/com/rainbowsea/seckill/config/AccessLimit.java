package com.rainbowsea.seckill.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AccessLimit {

    int second(); // 时间范围

    int maxCount(); // 访问的最大次数

    boolean needLogin() default true; // 是否登录

}
