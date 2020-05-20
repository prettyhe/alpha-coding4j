package com.alpha.coding.common.mybatis;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.alpha.coding.common.bean.register.EnableAutoRegistrar;

/**
 * EnableMybatisAutoConfigs
 *
 * @version 1.0
 * Date: 2020-03-21
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@EnableAutoRegistrar
@Import(AutoMybatisConfiguration.class)
public @interface EnableMybatisAutoConfigs {

    EnableMybatisAutoConfig[] value();

}
