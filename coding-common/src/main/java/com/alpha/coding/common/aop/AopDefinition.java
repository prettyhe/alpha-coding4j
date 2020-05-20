package com.alpha.coding.common.aop;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.alpha.coding.common.bean.register.EnableAutoRegistrar;

/**
 * AopDefinition
 *
 * @version 1.0
 * Date: 2020-03-20
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@EnableAutoRegistrar
@Repeatable(AopDefinitions.class)
public @interface AopDefinition {

    boolean proxyTargetClass() default false;

    String refBeanName();

    int order() default 0;

    String advice();

    String adviceMethod();

    /**
     * 切入点，最终会以空格拼接
     */
    String[] pointcut();

}
