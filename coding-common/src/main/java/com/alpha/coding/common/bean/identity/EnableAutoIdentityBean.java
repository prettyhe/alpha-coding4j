package com.alpha.coding.common.bean.identity;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;

import com.alpha.coding.common.bean.register.EnableAutoRegistrar;

/**
 * EnableAutoIdentityBean
 *
 * @version 1.0
 * Date: 2020/8/4
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@EnableAutoRegistrar
@Repeatable(EnableAutoIdentityBeans.class)
public @interface EnableAutoIdentityBean {

    /**
     * 扫描的包路径
     */
    String[] basePackages() default {};

    /**
     * 排除的类
     */
    Class[] excludes() default {};

    /**
     * Bean名称生成器
     */
    Class<? extends BeanNameGenerator> beanNameGenerator() default DefaultBeanNameGenerator.class;

}
