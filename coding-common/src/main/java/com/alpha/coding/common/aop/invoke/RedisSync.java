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
     * 请求计数key，SpEL表达式
     */
    String invokeCountKey() default "";

    /**
     * 计数区间内最大请求次数，支持外部化配置，如"${invoke.sync.maxInvokeTimes:5}"
     */
    String maxInvokeTimes() default "-1";

    /**
     * 请求计数时间区间，支持外部化配置，如"${invoke.sync.invokeCountTimeRange:86400}"
     * <li>TODAY:即当天有效</li>
     * <li>其它数值:从开始计数的秒数内有效</li>
     */
    String invokeCountTimeRange() default "";

    /**
     * 加锁过期时间(秒)，即预期锁定时长
     */
    long expireSeconds() default 5;

    /**
     * 是否快速失败，即未获取到锁时不阻塞等待，直接回调处理
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

    /**
     * 超过访问次数限制文案，支持外部化配置，如"${invoke.sync.exceedInvokeTimesText:次数超限}"
     */
    String exceedInvokeTimesText() default "";

}
