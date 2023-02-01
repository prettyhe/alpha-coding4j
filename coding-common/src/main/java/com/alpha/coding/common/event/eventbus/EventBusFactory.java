package com.alpha.coding.common.event.eventbus;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alpha.coding.bo.enums.util.EnumWithCodeSupplier;
import com.alpha.coding.common.event.AbstractEvent;
import com.alpha.coding.common.event.handler.EventHandleCallback;
import com.google.common.collect.Maps;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * EventBusFactory
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
@Component
public class EventBusFactory implements ApplicationContextAware, InitializingBean, EventHandleCallback {

    @Setter
    private ApplicationContext applicationContext;

    private Map<Class<? extends EnumWithCodeSupplier>, EventBus> eventBusMap = Maps.newHashMap();

    /**
     * 待处理事件计数器
     */
    private AtomicInteger eventToHandleCnt = new AtomicInteger(0);
    /**
     * 待处理事件计数明细
     */
    private ConcurrentMap<Class<? extends EnumWithCodeSupplier>, ConcurrentMap<EnumWithCodeSupplier, AtomicInteger>>
            eventToHandleCntMap = Maps.newConcurrentMap();

    @Override
    public void afterPropertiesSet() throws Exception {
        updateEventBusMap();
    }

    /**
     * 更新本地缓存的EventBus映射
     */
    public synchronized void updateEventBusMap() {
        Map<String, EventBus> beans = applicationContext.getBeansOfType(EventBus.class);
        beans.forEach((k, v) -> {
            eventBusMap.put(v.getIdentity(), v);
            if (log.isDebugEnabled()) {
                log.debug("register-bus: id={}, bus={}",
                        v.getIdentity().getName(), v.getClass().getName());
            }
        });
    }

    /**
     * 发送事件
     */
    public <K, E extends EnumWithCodeSupplier, AE extends AbstractEvent<K, E>> void post(AE event) {
        try {
            eventBusMap.get(event.getType().getClass()).post(event);
            eventToHandleCnt.incrementAndGet();
            incrementAndGet(event.getType());
        } catch (Exception e) {
            log.error("post event fail: event={}", JSON.toJSONString(event), e);
        }
    }

    private <E extends EnumWithCodeSupplier> void incrementAndGet(E type) {
        eventToHandleCntMap.computeIfAbsent(type.getClass(), p -> Maps.newConcurrentMap())
                .computeIfAbsent(type, p -> new AtomicInteger(0)).incrementAndGet();
    }

    private <E extends EnumWithCodeSupplier> void decrementAndGet(E type) {
        AtomicInteger atomicInteger =
                eventToHandleCntMap.computeIfAbsent(type.getClass(), p -> Maps.newConcurrentMap()).get(type);
        if (atomicInteger == null) {
            return;
        }
        atomicInteger.decrementAndGet();
    }

    @Override
    public <E extends EnumWithCodeSupplier> void afterHandle(String eventID, E type) {
        eventToHandleCnt.decrementAndGet();
        decrementAndGet(type);
    }

    /**
     * 获取待处理的事件个数
     */
    public int getWaitingEventCnt() {
        return eventToHandleCnt.get();
    }

    /**
     * 获取待处理事件个数详情
     */
    public Map<Class<? extends EnumWithCodeSupplier>, Map<EnumWithCodeSupplier, Integer>> getEventToHandleCntMap() {
        Map<Class<? extends EnumWithCodeSupplier>, Map<EnumWithCodeSupplier, Integer>> map = Maps.newHashMap();
        eventToHandleCntMap.forEach((k, v) -> {
            Map<EnumWithCodeSupplier, Integer> cntMap = Maps.newHashMap();
            v.forEach((x, y) -> cntMap.put(x, y.get()));
            map.put(k, cntMap);
        });
        return map;
    }

    /**
     * 等待事件处理完
     */
    public void holdUntilEventFinish() {
        while (!eventFinish()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean eventFinish() {
        int waitingEventCnt = getWaitingEventCnt();
        final Map<Class<? extends EnumWithCodeSupplier>, Map<EnumWithCodeSupplier, Integer>> eventToHandleCntMap =
                getEventToHandleCntMap();
        log.info("waitingEventCnt-> {}, eventToHandleCntMap-> {}", waitingEventCnt, eventToHandleCntMap);
        if (waitingEventCnt > 0) {
            return false;
        }
        for (Map.Entry<Class<? extends EnumWithCodeSupplier>, Map<EnumWithCodeSupplier, Integer>> entry :
                eventToHandleCntMap.entrySet()) {
            for (Map.Entry<EnumWithCodeSupplier, Integer> en : entry.getValue().entrySet()) {
                if (en.getValue() != null && en.getValue() > 0) {
                    return false;
                }
            }
        }
        return true;
    }

}
