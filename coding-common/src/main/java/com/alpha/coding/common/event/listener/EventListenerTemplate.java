/**
 * Copyright
 */
package com.alpha.coding.common.event.listener;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import com.alpha.coding.bo.enums.util.EnumWithCodeSupplier;
import com.alpha.coding.common.event.AbstractEvent;
import com.alpha.coding.common.event.common.ListenerChangeEvent;
import com.alpha.coding.common.event.common.MetaMonitor;
import com.alpha.coding.common.event.handler.EventHandlerFactory;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * EventListenerTemplate 事件监听模板类
 *
 * @param <E> 事件类别
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class EventListenerTemplate<E extends EnumWithCodeSupplier> implements EventListener<E>, InitializingBean,
        BeanFactoryPostProcessor {

    @Setter
    private String eventIdentityClassName;

    @Setter
    @Autowired
    protected EventHandlerFactory eventHandlerFactory;

    @Setter
    private Class<? extends EnumWithCodeSupplier> identity;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (identity == null) {
            identity = (Class<? extends EnumWithCodeSupplier>) Class.forName(eventIdentityClassName, true,
                    Thread.currentThread().getContextClassLoader());
        }
        if (log.isDebugEnabled()) {
            log.debug("init EventListenerTemplate for {}", this.identity.getName());
        }
    }

    @Override
    public Class<? extends EnumWithCodeSupplier> getIdentity() {
        return this.identity;
    }

    @Subscribe
    @AllowConcurrentEvents
    @Override
    public <K, AE extends AbstractEvent<K, E>> void listen(AE event) {
        onListening(event);
    }

    /**
     * 默认的事件处理
     * <p>
     * 子类可以覆盖，实现不同的处理
     * </p>
     */
    protected <K, AE extends AbstractEvent<K, E>> void onListening(AE event) {
        eventHandlerFactory.handleEvent(event);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        MetaMonitor.post(new ListenerChangeEvent().setListenerName(this.identity.getName()));
    }

}
