package com.alpha.coding.common.event.handler;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;

import com.alpha.coding.bo.enums.util.EnumWithCodeSupplier;
import com.alpha.coding.common.event.AbstractEventHandleResult;

import lombok.extern.slf4j.Slf4j;

/**
 * EventHandlerTemplate 事件处理器模板类
 *
 * @param <K> key类型
 * @param <E> 事件类别
 * @param <R> 处理结果类别
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public abstract class EventHandlerTemplate<K, E extends EnumWithCodeSupplier, R extends EnumWithCodeSupplier>
        implements EventHandler<K, E>, HandleStrategy<K, R>, InitializingBean {

    private static final String HANDLE_ERROR_LOG = "event-handle-error: {}";
    private Class handlerClass;

    @Override
    public void afterPropertiesSet() throws Exception {
        handlerClass = this.getClass();
    }

    @Override
    public Class<? extends EnumWithCodeSupplier> getIdentity() {
        return getEventType().getClass();
    }

    @Override
    public void handle(String eventID, Set<K> keys) {
        if (log.isDebugEnabled()) {
            log.debug("start-handle-event, eventID={}, handler={}, keys={}",
                    eventID, handlerClass.getSimpleName(), keys);
        }
        try {
            List<? extends AbstractEventHandleResult<K, R>> handleResults = handleWithStrategy(keys);
            if (handleResults == null || handleResults.isEmpty()) {
                return;
            }
            for (AbstractEventHandleResult<K, R> ret : handleResults) {
                log.error(getErrorLogTemplate(), ret.toMsg());
            }
        } finally {
            EventHandleCallback eventHandleCallback = getEventHandleCallback();
            if (eventHandleCallback != null) {
                eventHandleCallback.afterHandle(eventID, getEventType());
            }
        }
    }

    protected String getErrorLogTemplate() {
        return HANDLE_ERROR_LOG;
    }

    /**
     * 获取事件处理回调接口，子类实现
     */
    protected abstract EventHandleCallback getEventHandleCallback();

}
