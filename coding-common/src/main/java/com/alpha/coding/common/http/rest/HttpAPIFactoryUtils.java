package com.alpha.coding.common.http.rest;

import java.beans.Introspector;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.util.ClassUtils;

import com.alpha.coding.common.aop.assist.AopHelper;

import lombok.extern.slf4j.Slf4j;

/**
 * HttpAPIFactoryUtils
 *
 * @version 1.0
 * Date: 2020/7/8
 */
@Slf4j
public class HttpAPIFactoryUtils {

    public static void registerApi(BeanDefinitionRegistry beanDefinitionRegistry, BeanFactory beanFactory,
                                   MyRestTemplate restTemplate,
                                   Map<Class, String> apiMap, Map<Class, String> oldApiMap) {
        if (beanDefinitionRegistry == null || apiMap == null) {
            log.warn("registry or apiMap null");
            return;
        }
        Map<Class, String> apis = oldApiMap == null ? Collections.emptyMap() : oldApiMap;
        apiMap.forEach((apiClass, uri) -> {
            final String beanName = buildDefaultBeanName(apiClass.getName());
            if (beanDefinitionRegistry.containsBeanDefinition(beanName)
                    && uri.equals(apis.get(apiClass))) {
                return; // bean 属性未更新
            }
            final String handlerBeanName = beanName + "HttpAPIHandler";
            if (beanDefinitionRegistry.containsBeanDefinition(beanName)) {
                // 替换HttpAPIHandler uri属性
                final Object bean = getTargetBean(beanFactory.getBean(handlerBeanName));
                try {
                    final Field field = HttpAPIHandler.class.getDeclaredField("uri");
                    field.setAccessible(true);
                    field.set(bean, uri);
                } catch (Exception e) {
                    log.error("update HttpAPIHandler.uri fail for {}", apiClass.getName(), e);
                }
            } else {
                // 注册HttpAPIHandler bean
                BeanDefinitionBuilder handlerBeanDefinitionBuilder =
                        BeanDefinitionBuilder.genericBeanDefinition(HttpAPIHandler.class);
                handlerBeanDefinitionBuilder.addPropertyValue("restTemplate", restTemplate);
                handlerBeanDefinitionBuilder.addPropertyValue("uri", uri);
                beanDefinitionRegistry.registerBeanDefinition(handlerBeanName,
                        handlerBeanDefinitionBuilder.getBeanDefinition());
                // 注册API bean
                BeanDefinitionBuilder beanDefinitionBuilder =
                        BeanDefinitionBuilder.genericBeanDefinition(HttpAPIFactoryBean.class);
                beanDefinitionBuilder.addPropertyValue("interfaceType", apiClass);
                beanDefinitionBuilder.addPropertyReference("invocationHandler", handlerBeanName);
                beanDefinitionRegistry.registerBeanDefinition(beanName, beanDefinitionBuilder.getBeanDefinition());
            }
            log.info("create bean {} with uri {} for class {}", beanName, uri, apiClass.getName());
        });

    }

    private static String buildDefaultBeanName(String beanClassName) {
        String shortClassName = ClassUtils.getShortName(beanClassName);
        return Introspector.decapitalize(shortClassName);
    }

    private static Object getTargetBean(Object proxy) {
        try {
            return AopHelper.getTarget(proxy);
        } catch (Exception e) {
            log.error("getTarget fail for {}", proxy, e);
            return proxy;
        }
    }

}
