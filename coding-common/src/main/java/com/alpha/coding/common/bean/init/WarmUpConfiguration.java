package com.alpha.coding.common.bean.init;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alpha.coding.bo.executor.RunnableWrapper;

/**
 * WarmUpConfiguration
 *
 * @version 1.0
 * Date: 2020-03-19
 */
@Configuration
public class WarmUpConfiguration {

    @Bean
    public static RunnableWrapper noneRunnableWrapper() {
        return RunnableWrapper.none();
    }

    @Bean
    public static ContextWarmUpInitializer contextWarmUpInitializer() {
        return new ContextWarmUpInitializer(task -> noneRunnableWrapper());
    }

}
