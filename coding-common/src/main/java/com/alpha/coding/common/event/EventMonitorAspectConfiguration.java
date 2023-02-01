package com.alpha.coding.common.event;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alpha.coding.common.aop.AopDefinition;
import com.alpha.coding.common.event.aop.EventMonitorAspect;

/**
 * EventMonitorAspectConfiguration
 *
 * @version 1.0
 * Date: 2023/1/30
 */
@Configuration
@AopDefinition(proxyTargetClass = true, refBeanName = "defaultEventMonitorAspect",
        order = 12, advice = "around", adviceMethod = "doAspect",
        pointcut = {"@annotation(com.alpha.coding.common.event.annotations.EventMonitor)",
                "|| @annotation(com.alpha.coding.common.event.annotations.EventMonitor.List)"})
public class EventMonitorAspectConfiguration {

    @Bean("defaultEventMonitorAspect")
    public static EventMonitorAspect defaultEventMonitorAspect() {
        return new EventMonitorAspect();
    }

}
