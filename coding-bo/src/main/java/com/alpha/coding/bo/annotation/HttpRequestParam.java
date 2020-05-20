package com.alpha.coding.bo.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.alpha.coding.bo.enums.HttpParamFrom;

/**
 * HttpRequestParam
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface HttpRequestParam {

    /**
     * 参数名
     */
    String[] name() default {};

    /**
     * 参数类型
     */
    Class<?> javaType() default Object.class;

    /**
     * 参数来源排除
     */
    HttpParamFrom[] excludeFrom() default {};

}
