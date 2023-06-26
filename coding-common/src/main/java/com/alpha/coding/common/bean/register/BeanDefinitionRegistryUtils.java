package com.alpha.coding.common.bean.register;

import java.util.Objects;

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
        overideBeanDefinition(beanDefinitionRegistry, beanName, beanDefinition, false);
    }

    /**
     * 覆盖已有的bean定义
     *
     * @param beanDefinitionRegistry BeanDefinitionRegistry
     * @param beanName               beanName
     * @param beanDefinition         BeanDefinition
     * @param checkIsSameProperties  检查是否相同属性，相同属性时不覆盖
     * @return 是否覆盖了BeanDefinition
     */
    public static boolean overideBeanDefinition(BeanDefinitionRegistry beanDefinitionRegistry, String beanName,
                                                BeanDefinition beanDefinition, boolean checkIsSameProperties)
            throws NoSuchBeanDefinitionException, BeanDefinitionStoreException {
        if (!checkIsSameProperties
                || !hasSameBeanDefinitionByProperties(beanDefinitionRegistry, beanName, beanDefinition)) {
            if (beanDefinitionRegistry.containsBeanDefinition(beanName)) {
                beanDefinitionRegistry.removeBeanDefinition(beanName);
            }
            beanDefinitionRegistry.registerBeanDefinition(beanName, beanDefinition);
            return true;
        }
        return false;
    }

    /**
     * 是否存在相同beanName的相同的BeanDefinition，以BeanDefinition的properties维度判断
     *
     * @param beanDefinitionRegistry BeanDefinitionRegistry
     * @param beanName               beanName
     * @param beanDefinition         BeanDefinition
     */
    public static boolean hasSameBeanDefinitionByProperties(BeanDefinitionRegistry beanDefinitionRegistry,
                                                            String beanName, BeanDefinition beanDefinition) {
        if (!beanDefinitionRegistry.containsBeanDefinition(beanName)) {
            return false;
        }
        return Objects.equals(beanDefinitionRegistry.getBeanDefinition(beanName).getPropertyValues(),
                beanDefinition.getPropertyValues());
    }

}
