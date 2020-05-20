package com.alpha.coding.common.bean.comm;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * ApplicationContextHolder
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class ApplicationContextHolder implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextHolder.applicationContext = applicationContext;
    }

    public static ApplicationContext getCurrentApplicationContext() {
        return ApplicationContextHolder.applicationContext;
    }

    public static <T> T getBeanByType(Class<T> type) {
        return ApplicationContextHolder.applicationContext.getBean(type);
    }

    public static Object getBeanByName(String name) {
        return ApplicationContextHolder.applicationContext.getBean(name);
    }
}
