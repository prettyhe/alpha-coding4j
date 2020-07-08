package com.alpha.coding.common.http.rest;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.alpha.coding.common.bean.register.EnableAutoRegistrar;

/**
 * EnableAutoMyRestApis
 *
 * @version 1.0
 * Date: 2020/6/12
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@EnableAutoRegistrar
public @interface EnableAutoMyRestApis {

    EnableAutoMyRestApi[] value();

}
