package com.alpha.coding.common.event;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

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
@ComponentScan(basePackages = {"com.alpha.coding.common.event"},
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX,
                pattern = {"com.alpha.coding.common.event.*EventMonitorAspectConfiguration"}))
public class EventBusConfiguration {

    @Bean("defaultEventExecutor")
    public static MDCThreadPoolTaskExecutor mdcThreadPoolTaskExecutor() {
        final int availableProcessors = Runtime.getRuntime().availableProcessors();
        return MDCThreadPoolTaskExecutor.builder()
                .corePoolSize(Math.min(availableProcessors * 4, 16))
                .maxPoolSize(Math.min(availableProcessors * 4, 16))
                .keepAliveSeconds(300)
                .queueCapacity(200)
                .namedThreadFactory("DefaultEventExecutor")
                .rejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy())
                .build();
    }

    @Bean("defaultEventBusInstance")
    public static EventBus defaultEventBusInstance() {
        return new AsyncEventBus(mdcThreadPoolTaskExecutor());
    }

    @Bean("asyncEventBusAutoConfig")
    public static AsyncEventBusAutoConfig asyncEventBusAutoConfig() {
        return new AsyncEventBusAutoConfig();
    }

}
