package com.alpha.coding.common.bean.register;

import java.util.Objects;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

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
        if (!containsBeanDefinition(beanDefinitionRegistry, beanName)) {
            return false;
        }
        final BeanDefinition oldBeanDefinition = getBeanDefinition(beanDefinitionRegistry, beanName);
        return oldBeanDefinition != null
                && Objects.equals(oldBeanDefinition.getPropertyValues(), beanDefinition.getPropertyValues());
    }

    /**
     * 检测是否包含指定beanName的BeanDefinition
     */
    public static boolean containsBeanDefinition(BeanDefinitionRegistry beanDefinitionRegistry, String beanName) {
        if (beanDefinitionRegistry.containsBeanDefinition(beanName)) {
            return true;
        }
        return getBeanDefinition(beanDefinitionRegistry, beanName) != null;
    }

    /**
     * 获取指定beanName的BeanDefinition
     */
    public static BeanDefinition getBeanDefinition(BeanDefinitionRegistry beanDefinitionRegistry, String beanName) {
        if (beanDefinitionRegistry.containsBeanDefinition(beanName)) {
            return beanDefinitionRegistry.getBeanDefinition(beanName);
        }
        BeanDefinition beanDefinition = null;
        if (beanDefinitionRegistry instanceof DefaultListableBeanFactory) {
            final DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) beanDefinitionRegistry;
            if (beanFactory.containsBeanDefinition(beanName)) {
                try {
                    beanDefinition = beanFactory.getBeanDefinition(beanName);
                } catch (NoSuchBeanDefinitionException e) {
                    // nothing
                }
            }
            if (beanDefinition != null) {
                return beanDefinition;
            }
            final BeanFactory parentBeanFactory = beanFactory.getParentBeanFactory();
            if (parentBeanFactory instanceof BeanDefinitionRegistry) {
                beanDefinition = getBeanDefinition(((BeanDefinitionRegistry) parentBeanFactory), beanName);
            }
        }
        return beanDefinition;
    }

    /**
     * 移除指定beanName的BeanDefinition
     */
    public static void removeBeanDefinitionRecursive(BeanDefinitionRegistry beanDefinitionRegistry, String beanName) {
        if (beanDefinitionRegistry.containsBeanDefinition(beanName)) {
            beanDefinitionRegistry.removeBeanDefinition(beanName);
        }
        if (beanDefinitionRegistry instanceof DefaultListableBeanFactory) {
            final DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) beanDefinitionRegistry;
            if (beanFactory.containsBeanDefinition(beanName)) {
                try {
                    beanFactory.removeBeanDefinition(beanName);
                } catch (NoSuchBeanDefinitionException e) {
                    // nothing
                }
            }
            final BeanFactory parentBeanFactory = beanFactory.getParentBeanFactory();
            if (parentBeanFactory instanceof BeanDefinitionRegistry) {
                removeBeanDefinitionRecursive(((BeanDefinitionRegistry) parentBeanFactory), beanName);
            }
        }
    }

}
