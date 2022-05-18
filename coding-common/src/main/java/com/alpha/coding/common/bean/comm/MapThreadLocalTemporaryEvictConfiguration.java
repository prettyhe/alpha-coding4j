package com.alpha.coding.common.bean.comm;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alpha.coding.common.aop.AopDefinition;

/**
 * MapThreadLocalTemporaryEvictConfiguration
 *
 * @version 1.0
 * Date: 2022/5/8
 */
@Configuration
@AopDefinition(proxyTargetClass = true, refBeanName = "defaultMapThreadLocalTemporaryEvictAspect",
        order = 100, advice = "around", adviceMethod = "doAspect",
        pointcut = {
                "@within(com.alpha.coding.common.bean.comm.MapThreadLocalTemporaryEvict)",
                "|| @annotation(com.alpha.coding.common.bean.comm.MapThreadLocalTemporaryEvict)"
        })
public class MapThreadLocalTemporaryEvictConfiguration {

    @Bean("defaultMapThreadLocalTemporaryEvictAspect")
    public static MapThreadLocalTemporaryEvictAspect defaultMapThreadLocalTemporaryEvictAspect() {
        return new MapThreadLocalTemporaryEvictAspect();
    }

}
