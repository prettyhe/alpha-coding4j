package com.alpha.coding.common.aop.invoke;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alpha.coding.common.aop.AopDefinition;

/**
 * SyncInvokeConfiguration
 *
 * @version 1.0
 * Date: 2021/9/6
 */
@Configuration
@AopDefinition(proxyTargetClass = true, refBeanName = "syncInvokeAspect",
        order = 11, advice = "around", adviceMethod = "doAspect",
        pointcut = {
                "@within(com.alpha.coding.common.aop.invoke.SyncInvoke)",
                "|| @annotation(com.alpha.coding.common.aop.invoke.SyncInvoke)"
        })
public class SyncInvokeConfiguration {

    @Bean("syncInvokeAspect")
    public static SyncInvokeAspect syncInvokeAspect() {
        return new SyncInvokeAspect();
    }

}
