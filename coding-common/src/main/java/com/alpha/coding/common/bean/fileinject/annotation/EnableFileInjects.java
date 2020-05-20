package com.alpha.coding.common.bean.fileinject.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.alpha.coding.common.bean.register.EnableAutoRegistrar;

/**
 * EnableFileInjects
 *
 * @version 1.0
 * Date: 2020-03-18
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@EnableAutoRegistrar
public @interface EnableFileInjects {

    EnableFileInject[] value();

}
