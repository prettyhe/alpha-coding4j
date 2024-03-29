package com.alpha.coding.common.utils.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JSONObjectPath
 *
 * @version 1.0
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
public @interface JSONObjectPath {

    /**
     * 路径
     */
    String path();

    /**
     * 分隔符
     */
    String sep() default "\\.";

    /**
     * 类型
     */
    Class<?> javaType();

}
