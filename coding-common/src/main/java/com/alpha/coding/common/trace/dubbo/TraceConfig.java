package com.alpha.coding.common.trace.dubbo;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * TraceConfig
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Data
@Accessors(chain = true)
public class TraceConfig implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    private boolean enable;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        TraceConfig.applicationContext = applicationContext;
    }

    public static TraceConfig loadConfig() {
        return applicationContext == null ? null : applicationContext.getBean(TraceConfig.class);
    }

    public boolean enable() {
        return enable;
    }

}
