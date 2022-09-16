package com.alpha.coding.bo.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JsonPath
 *
 * @version 1.0
 * Date: 2022-08-18
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
public @interface JsonPath {

    /**
     * 路径
     */
    String path();

    /**
     * 忽略空值
     */
    boolean ignoreNullValue() default true;

}
