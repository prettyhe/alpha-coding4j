package com.alpha.coding.common.event;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.alpha.coding.common.bean.register.EnableAutoRegistrar;

/**
 * EnableDefaultEventMonitorAspect
 *
 * @version 1.0
 * Date: 2023-01-30
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@EnableAutoRegistrar
@Import(EventMonitorAspectConfiguration.class)
public @interface EnableDefaultEventMonitorAspect {

}
