package com.alpha.coding.common.event;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.alpha.coding.bo.enums.util.EnumWithCodeSupplier;
import com.alpha.coding.common.bean.register.EnableAutoRegistrar;

/**
 * EnableAutoConfigEventBus
 *
 * @version 1.0
 * Date: 2020-03-19
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@EnableAutoRegistrar
@Import(EventBusConfiguration.class)
@Repeatable(EnableAutoConfigEventBuss.class)
public @interface EnableAutoConfigEventBus {

    Class<? extends EnumWithCodeSupplier>[] eventIdentity() default {};

    boolean useDefaultBusInstance() default true;

    String eventBusInstanceName() default "";

}
