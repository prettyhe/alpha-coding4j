package com.alpha.coding.common.event.configuration;

import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.alpha.coding.bo.enums.util.EnumWithCodeSupplier;
import com.google.common.collect.Maps;

import lombok.Setter;

/**
 * EventConfigurationFactory
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Component
public class EventConfigurationFactory implements ApplicationContextAware, InitializingBean {

    @Setter
    private ApplicationContext applicationContext;

    /**
     * 事件通用配置
     */
    private Map<Class<? extends EnumWithCodeSupplier>, EventConfiguration> globalMap = Maps.newHashMap();
    /**
     * 事件下某些类型事件专用配置
     */
    private Map<Class<? extends EnumWithCodeSupplier>, Map<EnumWithCodeSupplier, EventConfiguration>> configureMap =
            Maps.newHashMap();

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, EventConfiguration> beans = applicationContext.getBeansOfType(EventConfiguration.class);
        beans.forEach((k, v) -> {
            if (v.getIdentity() == null) {
                return;
            }
            if (v.getEffectedEventTypeSet() == null) {
                globalMap.put(v.getIdentity(), v);
            } else {
                Map<EnumWithCodeSupplier, EventConfiguration> typeMap = Maps.newHashMap();
                v.getEffectedEventTypeSet().forEach(x -> typeMap.put(x, v));
                configureMap.put(v.getIdentity(), typeMap);
            }
        });
    }

    /**
     * 获取事件配置：优先获取匹配该时间类型的配置，其次全局配置，最后默认配置
     */
    public EventConfiguration getEventConfiguration(Class<? extends EnumWithCodeSupplier> eventIdentity,
                                                    EnumWithCodeSupplier eventType) {
        Map<EnumWithCodeSupplier, EventConfiguration> configurationMap = configureMap.get(eventIdentity);
        if (configurationMap != null) {
            EventConfiguration configuration = configurationMap.get(eventType);
            if (configuration != null) {
                return configuration;
            }
        }
        return getEventConfiguration(eventIdentity);
    }

    public EventConfiguration getEventConfiguration(Class<? extends EnumWithCodeSupplier> eventIdentity) {
        EventConfiguration eventConfiguration = globalMap.get(eventIdentity);
        return eventConfiguration == null ? EventConfiguration.DEFAULT : eventConfiguration;
    }

}
