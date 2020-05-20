/**
 * Copyright
 */
package com.alpha.coding.common.event.handler;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.alpha.coding.bo.enums.util.EnumWithCodeSupplier;
import com.alpha.coding.common.event.AbstractEvent;
import com.google.common.collect.Maps;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * EventHandlerFactory 事件处理器工厂类，自动组装事件处理器
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
@Component
public class EventHandlerFactory implements ApplicationContextAware, InitializingBean {

    @Setter
    private ApplicationContext applicationContext;

    private Map<Class<? extends EnumWithCodeSupplier>, Map<EnumWithCodeSupplier, EventHandler>> handlerMap =
            Maps.newHashMap();

    /**
     * 正在进行处理的事件个数
     */
    private AtomicInteger handlingEventCnt = new AtomicInteger(0);

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, EventHandler> beans = applicationContext.getBeansOfType(EventHandler.class);
        beans.forEach((k, v) -> {
            Map<EnumWithCodeSupplier, EventHandler> sameTypeEventHandlerMap = handlerMap.get(v.getIdentity());
            if (sameTypeEventHandlerMap == null) {
                sameTypeEventHandlerMap = Maps.newHashMap();
                handlerMap.put(v.getIdentity(), sameTypeEventHandlerMap);
            }
            sameTypeEventHandlerMap.put(v.getEventType(), v);
        });
    }

    /**
     * 从工厂中找到事件对应的EventHandler
     */
    public <K, E extends EnumWithCodeSupplier, AE extends AbstractEvent<K, E>> EventHandler getEventHandler(AE event) {
        return handlerMap.get(event.getType().getClass()).get(event.getType());
    }

    /**
     * 从工厂中找到事件对应的EventHandler进行处理
     */
    public <K, E extends EnumWithCodeSupplier, AE extends AbstractEvent<K, E>> void handleEvent(AE event) {
        if (event == null) {
            return;
        }
        handlingEventCnt.incrementAndGet();
        try {
            EventHandler eventHandler = handlerMap.get(event.getType().getClass()).get(event.getType());
            eventHandler.handle(event.getEventID(), event.getKeys());
        } catch (Exception e) {
            log.error("event-handle-fail, eventID={}", event.getEventID(), e);
        } finally {
            handlingEventCnt.decrementAndGet();
            if (log.isDebugEnabled()) {
                log.debug("event-receive-and-handle: eventID={}, type={}, keys={}, latency={}", event.getEventID(),
                        event.getType(), event.getKeys(), (System.currentTimeMillis() - event.getTimestamp()));
            }
        }
    }

    /**
     * 获取正在进行处理的事件个数
     */
    public int getHandlingEventCnt() {
        return handlingEventCnt.get();
    }

}
