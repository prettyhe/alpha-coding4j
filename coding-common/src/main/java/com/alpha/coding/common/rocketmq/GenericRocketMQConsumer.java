package com.alpha.coding.common.rocketmq;

import java.util.Objects;
import java.util.UUID;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.springframework.beans.factory.InitializingBean;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * GenericRocketMQConsumer
 *
 * @version 1.0
 * Date: 2021/4/28
 */
@Slf4j
public class GenericRocketMQConsumer extends DefaultMQPushConsumer implements InitializingBean {

    @Setter
    private String topic;
    @Setter
    private String tag = "*";

    @Override
    public void afterPropertiesSet() throws Exception {
        super.subscribe(this.topic, this.tag);
        if (Objects.equals(System.getProperty("rocketmq.client.name", "DEFAULT"), this.getInstanceName())) {
            this.setInstanceName(UUID.randomUUID().toString().replaceAll("-", ""));
        }
        log.info("RocketMQ 创建实例成功，主题={}，消费组={}，实例名为={}", this.topic, this.getConsumerGroup(),
                this.getInstanceName());
        super.start();
    }

}
