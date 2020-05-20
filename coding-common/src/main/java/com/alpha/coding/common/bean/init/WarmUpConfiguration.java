package com.alpha.coding.common.bean.init;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * WarmUpConfiguration
 *
 * @version 1.0
 * Date: 2020-03-19
 */
@Configuration
public class WarmUpConfiguration {

    @Bean
    public static ContextWarmUpInitializer contextWarmUpInitializer() {
        return new ContextWarmUpInitializer();
    }

}
