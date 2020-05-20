package com.alpha.coding.bo.executor.impl;

import java.util.UUID;

import org.slf4j.MDC;

import com.alpha.coding.bo.constant.Keys;
import com.alpha.coding.bo.executor.RunnableWrapper;

/**
 * UUIDTraceRunnableWrapper
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class UUIDTraceRunnableWrapper extends RunnableWrapper {

    public UUIDTraceRunnableWrapper(Runnable command) {
        super(command);
        final String[] traceIdArr = new String[1];
        this.setBefore(new Runnable() {
            @Override
            public void run() {
                if (MDC.getMDCAdapter() != null) {
                    traceIdArr[0] = MDC.get(Keys.TRACE_ID);
                    if (traceIdArr[0] == null) {
                        MDC.put(Keys.TRACE_ID, genTraceId());
                    }
                }
            }
        });
        this.setAfter(new Runnable() {
            @Override
            public void run() {
                if (MDC.getMDCAdapter() != null && traceIdArr[0] == null) {
                    MDC.remove(Keys.TRACE_ID);
                }
            }
        });
    }

    protected String genTraceId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

}
