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
     * 过期时间(秒)
     */
    long expireSeconds() default 5;

    /**
     * 快速失败
     */
    boolean failFast() default true;

    /**
     * 获取锁时最长等待时间（秒），在failFast=true时生效，支持外部化配置，如"${invoke.sync.maxWaitSeconds:5}"
     */
    String maxWaitSeconds() default "0";

    /**
     * 尝试获取锁的时间间隔（毫秒），在failFast=true时生效，默认100ms，支持外部化配置，如"${invoke.sync.tryLockIntervalMillis:100}"
     */
    String tryLockIntervalMillis() default "100";

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
