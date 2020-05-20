/**
 * Copyright
 */
package com.alpha.coding.common.event.listener;

import java.util.Collection;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.alpha.coding.bo.enums.util.EnumWithCodeSupplier;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import lombok.Setter;

/**
 * EventListenerFactory 事件监听工厂类
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Component
public class EventListenerFactory implements ApplicationContextAware, InitializingBean {

    @Setter
    private ApplicationContext applicationContext;

    /**
     * 一种类型的事件可能被多个监听器监听
     */
    private Multimap<Class<? extends EnumWithCodeSupplier>, EventListener> listenerMap = ArrayListMultimap.create();

    @Override
    public void afterPropertiesSet() throws Exception {
        updateListenerMap();
    }

    public synchronized void updateListenerMap() {
        Map<String, EventListener> beans = applicationContext.getBeansOfType(EventListener.class);
        beans.forEach((k, v) -> listenerMap.put(v.getIdentity(), v));
    }

    /**
     * 获取类型事件监听器
     */
    public Collection<EventListener> getEventListeners(Class<? extends EnumWithCodeSupplier> type) {
        return listenerMap.get(type);
    }

}
