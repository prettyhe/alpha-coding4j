package com.alpha.coding.common.bean.comm;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.alpha.coding.common.aop.assist.AopHelper;

import lombok.extern.slf4j.Slf4j;

/**
 * AutowireBeanProcessor
 *
 * @version 1.0
 * Date: 2020/8/4
 */
@Slf4j
public class AutowireBeanProcessor implements ApplicationContextAware, BeanPostProcessor,
        ApplicationListener<ContextRefreshedEvent> {

    private List<String> beanNames;
    private final List<String> failWhenBeforeInitializationBeanNames = new ArrayList<>();

    private ApplicationContext applicationContext;

    public void setBeanNames(List<String> beanNames) {
        this.beanNames = beanNames;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (beanNames != null && beanNames.contains(beanName)) {
            try {
                applicationContext.getAutowireCapableBeanFactory().autowireBean(AopHelper.getTarget(bean));
                if (log.isDebugEnabled()) {
                    log.debug("autowireBean for beanName={}, bean={}", beanName, bean);
                }
            } catch (Exception e) {
                failWhenBeforeInitializationBeanNames.add(beanName);
                log.warn("autowireBean fail for {}", beanName, e);
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        final AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
        for (String beanName : failWhenBeforeInitializationBeanNames) {
            try {
                final Object bean = beanFactory.getBean(beanName);
                beanFactory.autowireBean(AopHelper.getTarget(bean));
                if (log.isDebugEnabled()) {
                    log.debug("autowireBean for beanName={}, bean={}", beanName, bean);
                }
            } catch (Exception e) {
                log.warn("autowireBean fail for {}", beanName, e);
            }
        }
    }
}
