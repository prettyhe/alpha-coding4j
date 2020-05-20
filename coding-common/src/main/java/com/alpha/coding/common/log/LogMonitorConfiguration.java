package com.alpha.coding.common.log;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LogMonitorConfiguration
 *
 * @version 1.0
 * Date: 2020-03-20
 */
@Configuration
public class LogMonitorConfiguration {

    @Bean("defaultLogor")
    public static DefaultLogor defaultLogor() {
        return new DefaultLogor();
    }

    @Bean("annotationLogor")
    public static AnnotationLogor annotationLogor() {
        return new AnnotationLogor();
    }

}
