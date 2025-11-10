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
 * Date: 2019年11月13日
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
     * 阻断条件，SpEL表达式。为空或执行返回为true时阻断，默认不阻断
     */
    String blockCondition() default "false";

    /**
     * 启用条件，SpEL表达式。为空或执行返回为true时启用限制，默认启用
     */
    String condition() default "true";

    /**
     * 本地控制：并发数(大于1表示支持多并发处理)，支持外部化配置，如"${invoke.sync.concurrency:1}"
     */
    String concurrency() default "1";

    /**
     * 本地控制：竞争失败后快速失败，否则进入等待
     */
    boolean failFastWhenAcquireFail() default false;

    /**
     * 本地控制：最长等待时间(ms), -1表示无限等待
     */
    long maxAwait() default -1;

    /**
     * 本地控制：等待超时后快速失败
     */
    boolean failFastWhenTimeout() default false;

    /**
     * 本地控制：等待中断后快速失败
     */
    boolean failFastWhenWaitInterrupted() default false;

    /**
     * 本地控制：阻断/失败/等待超时/等待中断回调
     */
    Class<? extends FailCallback> failCallback() default FailCallback.class;

    /**
     * 本地控制：失败文案，支持外部化配置，如"${invoke.sync.failText:操作太频繁}"
     */
    String failText() default "";

    /**
     * 阻断文案，支持外部化配置，如"${invoke.sync.blockedText:接口禁用}"
     */
    String blockedText() default "";

    /**
     * 基于Redis的同步请求控制配置，默认不开启，开启时忽略本地配置
     */
    RedisSync redisSync() default @RedisSync(enable = false);

}
