package com.alpha.coding.common.bean.register;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

/**
 * BeanDefinitionRegistryUtils
 *
 * @version 1.0
 * Date: 2020/9/15
 */
public class BeanDefinitionRegistryUtils {

    /**
     * 覆盖已有的bean定义
     *
     * @param beanDefinitionRegistry BeanDefinitionRegistry
     * @param beanName               beanName
     * @param beanDefinition         BeanDefinition
     */
    public static void overideBeanDefinition(BeanDefinitionRegistry beanDefinitionRegistry, String beanName,
                                             BeanDefinition beanDefinition)
            throws NoSuchBeanDefinitionException, BeanDefinitionStoreException {
        if (beanDefinitionRegistry.containsBeanDefinition(beanName)) {
            beanDefinitionRegistry.removeBeanDefinition(beanName);
        }
        beanDefinitionRegistry.registerBeanDefinition(beanName, beanDefinition);
    }

}
