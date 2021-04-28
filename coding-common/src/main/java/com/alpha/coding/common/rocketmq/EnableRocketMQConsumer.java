package com.alpha.coding.common.rocketmq;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.alpha.coding.common.bean.register.EnableAutoRegistrar;

/**
 * EnableRocketMQConsumer
 *
 * <p>config like this</p>
 * <p>rocketmq.nameserver=nameserver1.rockeqmq.com:9876;nameserver2.rockeqmq.com:9876</p>
 * <p>rocketmq.consumer.pullInterval=1000</p>
 * <p>rocketmq.consumer.pullBatchSize=10</p>
 * <p>rocketmq.consumer.consumeMessageBatchMaxSize=100</p>
 * <p>rocketmq.consumer.consumeThreadMin=2</p>
 * <p>rocketmq.consumer.consumeThreadMax=8</p>
 *
 * @version 1.0
 * Date: 2021/4/28
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@EnableAutoRegistrar
@Repeatable(EnableRocketMQConsumers.class)
public @interface EnableRocketMQConsumer {

    /**
     * 消费者分组，支持SpEL表达式
     */
    String group();

    /**
     * topic，支持SpEL表达式
     */
    String topic();

    /**
     * tag，支持SpEL表达式
     */
    String tag() default "*";

    /**
     * 消息处理器BeanName
     */
    String messageListenerBeanName();

    /**
     * 消费者BeanName，为空时系统自动生成
     */
    String consumerBeanName() default "";

}
