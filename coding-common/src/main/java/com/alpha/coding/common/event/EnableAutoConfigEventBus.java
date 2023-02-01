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
 * <p>config like this</p>
 * <p>event.bus.polling.event.interval=200</p>
 * <p>event.bus.enable.async.post=true</p>
 * <p>event.bus.enable.event.post.monitor=true</p>
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

    /**
     * 事件类型
     */
    Class<? extends EnumWithCodeSupplier>[] eventIdentity() default {};

    /**
     * 是否使用默认事件总线实例
     */
    boolean useDefaultBusInstance() default true;

    /**
     * 事件总线bean名称
     */
    String eventBusInstanceName() default "";

    /**
     * 轮询拉取事件间隔(ms)，支持外部化配置，目标值为整型
     */
    String pollingEventInterval() default "${event.bus.polling.event.interval:200}";

    /**
     * 是否开启异步发送，支持外部化配置，目标值为布尔型
     */
    String enableAsyncPost() default "${event.bus.enable.async.post:true}";

    /**
     * 是否开启事件发送监控，支持外部化配置，目标值为布尔型
     */
    String enableEventPostMonitor() default "${event.bus.enable.event.post.monitor:true}";

}
