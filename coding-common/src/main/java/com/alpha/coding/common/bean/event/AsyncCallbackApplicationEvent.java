package com.alpha.coding.common.bean.event;

import java.util.Map;

import org.slf4j.MDC;
import org.springframework.context.ApplicationEvent;

import com.alpha.coding.common.executor.MDCRunnableWrapper;

import lombok.Getter;

/**
 * AsyncCallbackApplicationEvent
 *
 * @version 1.0
 * Date: 2021/6/29
 */
public class AsyncCallbackApplicationEvent extends ApplicationEvent {

    @Getter
    private String eventKey;

    @Getter
    private Map<String, String> mdcCopy;

    public AsyncCallbackApplicationEvent(Runnable runnable) {
        this(runnable, null);
    }

    public AsyncCallbackApplicationEvent(Runnable runnable, String eventKey) {
        super(new MDCRunnableWrapper(runnable, MDC.getMDCAdapter() == null ? null : MDC.getCopyOfContextMap()));
        this.eventKey = eventKey;
        if (MDC.getMDCAdapter() != null) {
            this.mdcCopy = MDC.getCopyOfContextMap();
        }
    }
}
