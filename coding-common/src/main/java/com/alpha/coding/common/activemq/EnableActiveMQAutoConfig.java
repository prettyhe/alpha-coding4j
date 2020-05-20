package com.alpha.coding.common.activemq;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.alpha.coding.common.bean.register.EnableAutoRegistrar;

/**
 * EnableAutoActiveMQ
 *
 * <p>config like this</p>
 * <p>activemq.user=user</p>
 * <p>activemq.password=password</p>
 * <p>activemq.broker-url=tcp://localhost:61616</p>
 * <p>activemq.pool.max-connections=2</p>
 * <p>activemq.topic.enables=topic-demo</p>
 * <p>activemq.topic.topic-demo.producer=topic-demo-name</p>
 * <p>activemq.topic.topic-demo.JmsTemplateName=topicDemoJmsTemplate</p>
 * <p>activemq.topic.topic-demo.consumer=topic-demo-name</p>
 * <p>activemq.topic.topic-demo.listenerBean=topicDemoListenerBeanName</p>
 * <p>activemq.topic.topic-demo.consumerConcurrency=1-1</p>
 * <p>activemq.queue.enables=queue-demo</p>
 * <p>activemq.queue.queue-demo.producer=queue-demo-name</p>
 * <p>activemq.queue.queue-demo.JmsTemplateName=queueDemoJmsTemplate</p>
 * <p>activemq.queue.queue-demo.consumer=queue-demo-name</p>
 * <p>activemq.queue.queue-demo.listenerBean=queueDemoListenerBeanName</p>
 * <p>activemq.queue.queue-demo.consumerConcurrency=2-4</p>
 *
 * @version 1.0
 * Date: 2020/4/4
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@EnableAutoRegistrar
@Repeatable(EnableActiveMQAutoConfigs.class)
public @interface EnableActiveMQAutoConfig {

    /**
     * 配置文件前缀，默认activemq
     */
    String[] prefix() default {"activemq"};

}
