package com.alpha.coding.common.log;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.alpha.coding.common.bean.register.EnableAutoRegistrar;

/**
 * EnableLogMonitor
 *
 * @version 1.0
 * Date: 2020-03-20
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@EnableAutoRegistrar
@Repeatable(EnableLogMonitors.class)
@Import(LogMonitorConfiguration.class)
public @interface EnableLogMonitor {

    /**
     * beanName of com.alpha.coding.common.log
     */
    String logorBean() default "defaultLogor";

    /**
     * 日志配置
     */
    LogMonitor logConfig();

    /**
     * AOP: proxyTargetClass
     */
    boolean proxyTargetClass() default true;

    /**
     * AOP: order
     */
    int order() default 0;

    /**
     * AOP: pointcut，最终会以空格拼接
     */
    String[] pointcut();

}
