package com.alpha.coding.common.bean.spi;

import java.util.Comparator;

import com.google.common.collect.Multimap;

public class DefaultConfigurationRegisterHandler implements ConfigurationRegisterHandler {

    private static final String HANDLER_DIR = "META-INF/coding4j/";

    @Override
    public void registerBeanDefinitions(RegisterBeanDefinitionContext context) {
        final Multimap<String, ConfigurationRegisterHandler> handlerMap =
                ServiceBootstrap.loadAllOrdered(HANDLER_DIR, ConfigurationRegisterHandler.class);
        if (handlerMap != null) {
            // 同一类的只取第一个Handler处理，所有Handler会再排序处理
            handlerMap.keySet().stream()
                    .map(k -> handlerMap.get(k).iterator().next())
                    .sorted(Comparator.comparingInt(x -> x.getOrder()))
                    .forEach(x -> x.registerBeanDefinitions(context));
        }
    }

    @Override
    public int getOrder() {
        return -255;
    }
}
