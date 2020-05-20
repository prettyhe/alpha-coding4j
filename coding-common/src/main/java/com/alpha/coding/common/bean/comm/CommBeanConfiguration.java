package com.alpha.coding.common.bean.comm;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import com.alpha.coding.common.bean.identity.IdentityBeanFactory;
import com.alpha.coding.common.executor.MDCThreadPoolTaskExecutor;
import com.alpha.coding.common.utils.xls.XLSCellHandlerRegister;

/**
 * CommBeanConfiguration
 *
 * @version 1.0
 * Date: 2020-03-19
 */
@Configuration
public class CommBeanConfiguration {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer =
                new PropertySourcesPlaceholderConfigurer();
        propertySourcesPlaceholderConfigurer.setIgnoreUnresolvablePlaceholders(true);
        return propertySourcesPlaceholderConfigurer;
    }

    @Bean
    public static XLSCellHandlerRegister xlsCellHandlerRegister() {
        return new XLSCellHandlerRegister();
    }

    @Bean
    public static PropertiesStringValueResolver propertiesStringValueResolver() {
        return new PropertiesStringValueResolver();
    }

    @Bean
    public static EnvHolder envHolder() {
        return new EnvHolder();
    }

    @Bean
    public static ApplicationContextHolder applicationContextHolder() {
        return new ApplicationContextHolder();
    }

    @Bean
    public static InjectSelfRefBeanProcessor injectSelfRefBeanProcessor() {
        return new InjectSelfRefBeanProcessor();
    }

    @Lazy
    @Bean("bizExecutor")
    public static MDCThreadPoolTaskExecutor bizMDCThreadPoolTaskExecutor() {
        final int availableProcessors = Runtime.getRuntime().availableProcessors();
        return MDCThreadPoolTaskExecutor.builder()
                .corePoolSize(availableProcessors * 2)
                .maxPoolSize(availableProcessors * 2)
                .keepAliveSeconds(300)
                .namedThreadFactory("bizExecutor")
                .build();
    }

    @Bean
    public static IdentityBeanFactory identityBeanFactory() {
        return new IdentityBeanFactory();
    }

}
