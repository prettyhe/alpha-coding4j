package com.alpha.coding.common.event.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.alpha.coding.common.event.parser.DefaultKeyParser;
import com.alpha.coding.common.event.parser.EventKeyFrom;
import com.alpha.coding.common.event.parser.EventKeyParser;

/**
 * EventMonitor
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EventMonitor {

    /**
     * 事件类型
     */
    EventType eventType();

    /**
     * 事件的key来源,默认从请求参数里面获取
     */
    EventKeyFrom keyFrom() default EventKeyFrom.REQUEST;

    /**
     * 获取key的参数在请求参数中位置
     */
    int keyParamOrder() default 0;

    /**
     * 请求参数获取key的回调(回调参数为指定位置的参数)
     */
    Class<? extends EventKeyParser> requestKeyParser() default DefaultKeyParser.class;

    /**
     * 返回结果获取key的回调(回调参数为返回结果)
     */
    Class<? extends EventKeyParser> returnKeyParser() default DefaultKeyParser.class;

    /**
     * 自定义获取key的回调(回调参数为{ParseSrcWrapper})
     */
    Class<? extends EventKeyParser> customKeyParser() default DefaultKeyParser.class;

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {

        EventMonitor[] value();

    }

}
