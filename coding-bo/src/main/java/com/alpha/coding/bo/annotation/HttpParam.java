package com.alpha.coding.bo.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.web.bind.annotation.Mapping;

/**
 * HttpParam
 *
 * @version 1.0
 * Date: 2019-12-27
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
@Documented
@Mapping
public @interface HttpParam {

    /**
     * 参数名
     */
    String[] name() default {};

}
