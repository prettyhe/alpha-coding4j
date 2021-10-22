package com.alpha.coding.common.message;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.alpha.coding.common.bean.register.EnableAutoRegistrar;

/**
 * EnableMessageMonitorSend 使能消息发送监控
 *
 * @version 1.0
 * Date: 2021/9/8
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@EnableAutoRegistrar
@Import(MessageConfiguration.class)
public @interface EnableMessageMonitorSend {

    /**
     * 业务分组
     */
    String bizGroup();

    /**
     * DataSource beanName
     */
    String dataSource();

    /**
     * RedisTemplate beanName
     */
    String redisTemplate();

    /**
     * MessagePublishAdaptor beanName
     */
    String messagePublishAdaptor();

    /**
     * 是否启用补偿任务
     */
    boolean enableCompensateTask() default true;

    /**
     * 补偿任务同步执行，分布式场景使用锁竞争
     */
    boolean synchronizedCompensateTask() default false;

}
