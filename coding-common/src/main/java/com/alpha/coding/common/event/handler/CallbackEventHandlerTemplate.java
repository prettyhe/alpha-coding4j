package com.alpha.coding.common.event.handler;

import org.springframework.beans.factory.annotation.Autowired;

import com.alpha.coding.bo.enums.util.EnumWithCodeSupplier;
import com.alpha.coding.common.event.eventbus.EventBusFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * CallbackEventHandlerTemplate 使用默认事件处理完成回调的事件处理模板类
 *
 * @param <K> key类型
 * @param <E> 事件类别
 * @param <R> 处理结果类别
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public abstract class CallbackEventHandlerTemplate<K, E extends EnumWithCodeSupplier, R extends EnumWithCodeSupplier>
        extends EventHandlerTemplate<K, E, R> implements EventHandleCallback {

    @Autowired
    private EventBusFactory eventBusFactory;

    @Override
    public <E extends EnumWithCodeSupplier> void afterHandle(String eventID, E type) {
        eventBusFactory.afterHandle(eventID, type);
    }

    @Override
    protected EventHandleCallback getEventHandleCallback() {
        return this;
    }
}
