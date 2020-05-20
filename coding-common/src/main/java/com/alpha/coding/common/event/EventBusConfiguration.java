package com.alpha.coding.common.event;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.alpha.coding.common.executor.MDCThreadPoolTaskExecutor;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;

/**
 * EventBusConfiguration
 *
 * @version 1.0
 * Date: 2020-03-19
 */
@Configuration
@ComponentScan(basePackages = {"com.alpha.coding.common.event"})
public class EventBusConfiguration {

    @Bean("defaultEventExecutor")
    public static MDCThreadPoolTaskExecutor mdcThreadPoolTaskExecutor() {
        final int availableProcessors = Runtime.getRuntime().availableProcessors();
        return MDCThreadPoolTaskExecutor.builder()
                .corePoolSize(availableProcessors * 2)
                .maxPoolSize(availableProcessors * 2)
                .keepAliveSeconds(300)
                .namedThreadFactory("DefaultEventExecutor")
                .build();
    }

    @Bean("defaultEventBusInstance")
    public static EventBus defaultEventBusInstance() {
        return new AsyncEventBus(mdcThreadPoolTaskExecutor());
    }

    @Bean
    public static AsyncEventBusAutoConfig asyncEventBusAutoConfig() {
        return new AsyncEventBusAutoConfig();
    }

}
