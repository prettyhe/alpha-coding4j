package com.alpha.coding.common.log;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * LogMonitor
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface LogMonitor {

    /**
     * log类型，默认是对外服务
     */
    LogType logType() default LogType.SERV_OUT;

    /**
     * 自定义log类型
     */
    String customLogType() default "";

    /**
     * 方法的请求参数是否需要打印到log中，默认不打印
     */
    boolean logRequest() default false;

    /**
     * 方法执行的结果是否需要打印，默认为false不打印
     */
    boolean logResponse() default false;

    /**
     * 方法的请求参数是否延迟打印到log中，默认不延迟
     */
    boolean lazyFormatRequest() default false;

    /**
     * 使用自身的log实例
     */
    boolean useItsLog() default false;

    /**
     * 需要排除的打印日志中的字段
     */
    String[] excludeInfoKeys() default {"threadName"};

    /**
     * 附加消息提供者的类型
     */
    Class<? extends ExtraMsgSupplier> extraMsgSupplier() default ThreadLocalExtraMsgSupplier.class;

    /**
     * 日志数据路径配置
     */
    LogDataPath logDataPath() default @LogDataPath;
}
