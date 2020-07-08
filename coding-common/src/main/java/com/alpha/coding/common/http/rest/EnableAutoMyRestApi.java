package com.alpha.coding.common.http.rest;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.alpha.coding.common.bean.register.EnableAutoRegistrar;

/**
 * EnableAutoMyRestApi
 *
 * @version 1.0
 * Date: 2020/6/12
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@EnableAutoRegistrar
@Repeatable(EnableAutoMyRestApis.class)
public @interface EnableAutoMyRestApi {

    /**
     * 前缀，配置文件前缀，未指定则必须指定restTemplateRef
     */
    String prefix() default "";

    /**
     * MyRestTemplate的beanName，未指定则必须指定prefix
     */
    String restTemplateRef() default "";

    /**
     * 接口集
     */
    Class[] apiClasses() default {};

    /**
     * 接口扫描的包
     */
    String[] scanBasePackages() default {};

    /**
     * HttpAPIFactory的beanName
     */
    String httpAPIFactoryBeanName() default "httpAPIFactory";

}
