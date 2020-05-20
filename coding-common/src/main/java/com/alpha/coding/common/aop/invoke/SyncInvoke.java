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
     * 最长等待时间,-1表示无限等待
     */
    long maxAwait() default -1;
}
