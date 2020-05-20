package com.alpha.coding.common.utils.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JSONObjectPath
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
public @interface JSONObjectPath {

    String path();

    String sep() default "\\.";

    Class<?> javaType();

}
