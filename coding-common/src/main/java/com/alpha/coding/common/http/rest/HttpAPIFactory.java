package com.alpha.coding.common.http.rest;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * HttpAPIFactory.java
 *
 * @author nick
 * @version 1.0
 * Date: 2018-04-24
 */
@Data
@Slf4j
@AllArgsConstructor
@RequiredArgsConstructor
public class HttpAPIFactory implements BeanFactoryPostProcessor {
    @NonNull
    private Map<Class<?>, String> apis;
    private Map<String, Object> nameBeanMap = new HashMap<>();
    @NonNull
    private MyRestTemplate restTemplate;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (apis != null && apis.size() > 0) {
            apis.forEach((apiClass, uri) -> {
                log.info("creating bean {} use uri {}", apiClass, uri);
                HttpAPIHandler handler = new HttpAPIHandler(restTemplate, uri);
                Object api = Proxy.newProxyInstance(HttpAPIFactory.class.getClassLoader(),
                        new Class[] {apiClass}, handler);
                nameBeanMap.put(apiClass.getSimpleName(), api);
                beanFactory.registerSingleton(apiClass.getSimpleName(), api);
            });
        }
    }

}
