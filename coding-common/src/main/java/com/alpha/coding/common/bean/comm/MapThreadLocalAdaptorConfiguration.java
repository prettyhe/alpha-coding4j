package com.alpha.coding.common.bean.comm;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alpha.coding.bo.base.MapThreadLocalMirrorAspect;
import com.alpha.coding.common.aop.AopDefinition;

/**
 * MapThreadLocalAdaptorConfiguration
 *
 * @version 1.0
 * Date: 2020-03-20
 */
@Configuration
@AopDefinition(proxyTargetClass = true, refBeanName = "defaultMapThreadLocalMirrorAspect",
        order = 10000, advice = "before", adviceMethod = "doBefore",
        pointcut = {
                "@within(com.alpha.coding.bo.annotation.MapThreadLocalMirror)",
                "|| @annotation(com.alpha.coding.bo.annotation.MapThreadLocalMirror)"
        })
@AopDefinition(proxyTargetClass = true, refBeanName = "defaultMapThreadLocalMirrorAspect",
        order = 10000, advice = "after", adviceMethod = "doAfter",
        pointcut = {
                "@within(com.alpha.coding.bo.annotation.MapThreadLocalMirror)",
                "|| @annotation(com.alpha.coding.bo.annotation.MapThreadLocalMirror)"
        })
public class MapThreadLocalAdaptorConfiguration {

    @Bean("defaultMapThreadLocalMirrorAspect")
    public static MapThreadLocalMirrorAspect mapThreadLocalMirrorAspect() {
        return new MapThreadLocalMirrorAspect();
    }

}
