package com.alpha.coding.common.rocketmq;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.alpha.coding.common.bean.register.EnableAutoRegistrar;

/**
 * EnableRocketMQProducer
 *
 * <p>config like this</p>
 * <p>rocketmq.nameserver=nameserver1.rockeqmq.com:9876;nameserver2.rockeqmq.com:9876</p>
 * <p>rocketmq.producer.retryTimesWhenSendFailed=3</p>
 *
 * @version 1.0
 * Date: 2021/4/28
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@EnableAutoRegistrar
@Repeatable(EnableRocketMQProducers.class)
public @interface EnableRocketMQProducer {

    /**
     * nameserver地址，支持SpEL表达式
     */
    String[] namesrvAddr() default {"${rocketmq.nameserver}"};

    /**
     * 生产者分组，支持SpEL表达式
     */
    String group();

    /**
     * producer BeanName
     */
    String producerBeanName() default "rocketMQProducer";

}
