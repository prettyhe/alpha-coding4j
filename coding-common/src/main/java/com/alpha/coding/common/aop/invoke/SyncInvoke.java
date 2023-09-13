package com.alpha.coding.common.aop.invoke;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * SyncInvoke
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface SyncInvoke {

    /**
     * key，SpEL表达式
     */
    String key() default "";

    /**
     * 并发数(大于1表示支持多并发处理)，支持外部化配置，如"${invoke.sync.concurrency:1}"
     */
    String concurrency() default "1";

    /**
     * 竞争失败后快速失败，否则进入等待
     */
    boolean failFastWhenAcquireFail() default false;

    /**
     * 最长等待时间(ms), -1表示无限等待
     */
    long maxAwait() default -1;

    /**
     * 等待超时后快速失败
     */
    boolean failFastWhenTimeout() default false;

    /**
     * 等待中断后快速失败
     */
    boolean failFastWhenWaitInterrupted() default false;

    /**
     * 失败/等待超时/等待中断回调
     */
    Class<? extends FailCallback> failCallback() default FailCallback.class;

    /**
     * 失败文案，支持外部化配置，如"${invoke.sync.failText:操作太频繁}"
     */
    String failText() default "";

    /**
     * Redis同步配置，默认不开启，开启时忽略本地配置
     */
    RedisSync redisSync() default @RedisSync(enable = false);
}
