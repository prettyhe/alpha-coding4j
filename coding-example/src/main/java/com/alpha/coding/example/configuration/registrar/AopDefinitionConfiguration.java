package com.alpha.coding.example.configuration.registrar;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alpha.coding.common.aop.AopDefinition;
import com.alpha.coding.common.log.LogEnvAspect;

/**
 * AopDefinitionConfiguration
 *
 * @version 1.0
 * Date: 2020-03-22
 */
@Configuration
@AopDefinition(proxyTargetClass = true, refBeanName = "logEnvAspect",
        order = -10, advice = "around", adviceMethod = "doAspect",
        pointcut = {
                "@within(org.springframework.stereotype.Controller)",
                "|| @annotation(org.springframework.stereotype.Controller)",
                "|| @within(org.springframework.web.bind.annotation.RestController)",
                "|| @annotation(org.springframework.web.bind.annotation.RestController)"
        })
public class AopDefinitionConfiguration {

    @Bean("logEnvAspect")
    public LogEnvAspect logEnvAspect() {
        return new LogEnvAspect().setKeys(Arrays.asList("system", "host", "port", "appName", "module"));
    }

}
