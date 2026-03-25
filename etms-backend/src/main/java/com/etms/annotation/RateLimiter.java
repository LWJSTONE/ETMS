package com.etms.annotation;

import java.lang.annotation.*;

/**
 * 限流注解
 * 用于标记需要限流的方法，防止恶意请求
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter {
    
    /**
     * 限流key前缀
     */
    String key() default "";
    
    /**
     * 时间窗口内最大请求次数
     */
    int limit() default 5;
    
    /**
     * 时间窗口大小（秒）
     */
    int period() default 60;
    
    /**
     * 限流提示消息
     */
    String message() default "请求过于频繁，请稍后再试";
}
