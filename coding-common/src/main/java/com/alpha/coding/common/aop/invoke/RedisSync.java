package com.alpha.coding.common.aop.invoke;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RedisSync
 *
 * @version 1.0
 * Date: 2020/7/7
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface RedisSync {

    /**
     * 是否启用
     */
    boolean enable() default false;

    /**
     * 过期时间
     */
    long expireSeconds() default 5;

    /**
     * 快速失败
     */
    boolean failFast() default true;

    /**
     * RedisTemplate的beanName
     */
    String redisTemplateBean() default "stringRedisTemplate";

}
