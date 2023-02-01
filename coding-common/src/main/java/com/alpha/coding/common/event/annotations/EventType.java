package com.alpha.coding.common.event.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.BiFunction;

import com.alpha.coding.bo.enums.util.EnumWithCodeSupplier;
import com.alpha.coding.common.event.common.EnumEventTypeParser;

/**
 * EventType
 *
 * @version 1.0
 * Date: 2020-02-19
 */
@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EventType {

    /**
     * 事件类型枚举的Class类型
     */
    Class<? extends EnumWithCodeSupplier> eventClass();

    /**
     * 事件类型枚举的名称
     */
    String type();

    /**
     * 字符串类型解析成对应事件类型的函数
     */
    Class<? extends BiFunction<Class<? extends EnumWithCodeSupplier>, String, EnumWithCodeSupplier>> typeParser() default EnumEventTypeParser.class;

}
