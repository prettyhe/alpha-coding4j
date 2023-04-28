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
     * 是否启用，默认启用
     */
    boolean enable() default true;

    /**
     * 过期时间
     */
    long expireSeconds() default 5;

    /**
     * 快速失败
     */
    boolean failFast() default true;

    /**
     * 限流令牌阈值，支持外部化配置，如"${invoke.sync.rateLimit:1}"
     */
    String rateLimit() default "1";

    /**
     * RedisTemplate的beanName
     */
    String redisTemplateBean() default "stringRedisTemplate";

    /**
     * 失败回调
     */
    Class<? extends FailCallback> failCallback() default FailCallback.class;

    /**
     * 失败文案，支持外部化配置，如"${invoke.sync.failText:操作太频繁}"
     */
    String failText() default "";

}
