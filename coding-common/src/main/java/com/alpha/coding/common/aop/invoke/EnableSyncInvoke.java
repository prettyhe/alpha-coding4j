package com.alpha.coding.common.aop.invoke;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.alpha.coding.common.bean.register.EnableAutoRegistrar;

/**
 * EnableSyncInvoke
 *
 * @version 1.0
 * Date: 2021/9/6
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@EnableAutoRegistrar
@Import(SyncInvokeConfiguration.class)
public @interface EnableSyncInvoke {
}
