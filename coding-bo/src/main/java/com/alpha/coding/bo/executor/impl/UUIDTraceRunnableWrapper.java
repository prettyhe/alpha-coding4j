package com.alpha.coding.bo.executor.impl;

import com.alpha.coding.bo.trace.UUIDTraceIdGenerator;

/**
 * UUIDTraceRunnableWrapper
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class UUIDTraceRunnableWrapper extends TraceRunnableWrapper {

    public UUIDTraceRunnableWrapper(Runnable command) {
        super(command, UUIDTraceIdGenerator.getInstance());
    }

}
