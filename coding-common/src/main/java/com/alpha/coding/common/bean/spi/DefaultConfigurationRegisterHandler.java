package com.alpha.coding.common.bean.spi;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.alpha.coding.common.bean.register.EnvironmentChangeEvent;
import com.alpha.coding.common.bean.register.EnvironmentChangeListener;
import com.google.common.collect.Multimap;

public class DefaultConfigurationRegisterHandler implements ConfigurationRegisterHandler, EnvironmentChangeListener {

    private static final String HANDLER_DIR = "META-INF/coding4j/";

    private List<ConfigurationRegisterHandler> registerHandlers;

    @Override
    public void registerBeanDefinitions(RegisterBeanDefinitionContext context) {
        final Multimap<String, ConfigurationRegisterHandler> handlerMap =
                ServiceBootstrap.loadAllOrdered(HANDLER_DIR, ConfigurationRegisterHandler.class);
        if (handlerMap != null) {
            // 同一类的只取第一个Handler处理，所有Handler会再排序处理
            registerHandlers = handlerMap.keySet().stream()
                    .map(k -> handlerMap.get(k).iterator().next())
                    .sorted(Comparator.comparingInt(ConfigurationRegisterHandler::getOrder))
                    .collect(Collectors.toList());
            registerHandlers.forEach(x -> x.registerBeanDefinitions(context));
        }
    }

    @Override
    public int getOrder() {
        return -255;
    }

    @Override
    public void onChange(EnvironmentChangeEvent event) {
        Optional.ofNullable(registerHandlers).ifPresent(hs -> hs.forEach(h -> {
            if (h instanceof EnvironmentChangeListener) {
                EnvironmentChangeListener listener = (EnvironmentChangeListener) h;
                listener.onChange(event);
            }
        }));
    }
}
