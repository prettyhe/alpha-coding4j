package com.alpha.coding.common.bean.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ApplicationEventConfiguration
 *
 * @version 1.0
 * Date: 2021/9/9
 */
@Configuration
public class ApplicationEventConfiguration {

    @Bean
    public static ApplicationEventListener applicationEventListener() {
        return new ApplicationEventListener();
    }

    @Bean
    public static ApplicationEventPublishDelegate applicationEventPublishDelegate(
            @Autowired ApplicationEventPublisher applicationEventPublisher) {
        final ApplicationEventPublishDelegate delegate = new ApplicationEventPublishDelegate();
        delegate.setApplicationEventPublisher(applicationEventPublisher);
        return delegate;
    }

}
