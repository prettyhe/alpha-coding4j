package com.alpha.coding.common.message;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.alpha.coding.common.bean.init.EnableAutoWarmUp;
import com.alpha.coding.common.executor.MDCThreadPoolTaskExecutor;

/**
 * MessageConfiguration
 *
 * @version 1.0
 * Date: 2021/9/8
 */
@Configuration
@EnableAutoWarmUp
@ComponentScan(basePackageClasses = {MessageConfiguration.class})
public class MessageConfiguration {

    @Lazy
    @Bean("messageApplicationEventHandlerExec")
    public static ExecutorService messageApplicationEventHandlerExec() {
        return MDCThreadPoolTaskExecutor.builder()
                .corePoolSize(Runtime.getRuntime().availableProcessors())
                .maxPoolSize(Runtime.getRuntime().availableProcessors())
                .keepAliveSeconds(300)
                .queueCapacity(500)
                .namedThreadFactory("MessageEventHandlerExec")
                .rejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy())
                .build();
    }

}
