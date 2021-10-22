package com.alpha.coding.common.rocketmq;

import java.util.Objects;
import java.util.UUID;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.springframework.beans.factory.DisposableBean;
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
public class GenericRocketMQConsumer extends DefaultMQPushConsumer implements InitializingBean, DisposableBean {

    @Setter
    private String topic;
    @Setter
    private String tag = "*";
    @Setter
    private String description;

    private volatile boolean started = false;

    @Override
    public void start() throws MQClientException {
        if (!started) {
            super.subscribe(this.topic, this.tag);
            if (Objects.equals(System.getProperty("rocketmq.client.name", "DEFAULT"), this.getInstanceName())) {
                this.setInstanceName(UUID.randomUUID().toString().replaceAll("-", ""));
            }
            log.info("[{}]创建RocketMQ实例,主题={},消费组={},实例名={}", this.description, this.topic,
                    this.getConsumerGroup(), this.getInstanceName());
            super.start();
            started = true;
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
        started = false;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }

    @Override
    public void destroy() throws Exception {
        shutdown();
    }

    public boolean isStarted() {
        return started;
    }

}
