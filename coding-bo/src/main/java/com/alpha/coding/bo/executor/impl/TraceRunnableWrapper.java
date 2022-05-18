package com.alpha.coding.bo.executor.impl;

import org.slf4j.MDC;

import com.alpha.coding.bo.constant.Keys;
import com.alpha.coding.bo.executor.RunnableWrapper;
import com.alpha.coding.bo.trace.TraceIdGenerator;

public class TraceRunnableWrapper extends RunnableWrapper {

    private TraceIdGenerator traceIdGenerator;

    public TraceRunnableWrapper() {
        init();
    }

    public TraceRunnableWrapper(Runnable command) {
        super(command);
        init();
    }

    public TraceRunnableWrapper(TraceIdGenerator traceIdGenerator) {
        this.traceIdGenerator = traceIdGenerator;
        init();
    }

    public TraceRunnableWrapper(Runnable command, TraceIdGenerator traceIdGenerator) {
        super(command);
        this.traceIdGenerator = traceIdGenerator;
        init();
    }

    private void init() {
        final String[] traceIdArr = new String[1];
        this.setBefore(() -> {
            if (MDC.getMDCAdapter() != null) {
                traceIdArr[0] = MDC.get(Keys.TRACE_ID);
                if (traceIdArr[0] == null && traceIdGenerator != null) {
                    MDC.put(Keys.TRACE_ID, traceIdGenerator.traceId());
                }
            }
        });
        this.setAfter(() -> {
            if (MDC.getMDCAdapter() != null && traceIdArr[0] == null) {
                MDC.remove(Keys.TRACE_ID);
            }
        });
    }

}
