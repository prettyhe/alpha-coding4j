package com.alpha.coding.bo.trace;

import java.util.UUID;

import lombok.Getter;

/**
 * UUIDTraceIdGenerator
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class UUIDTraceIdGenerator implements TraceIdGenerator {

    @Getter
    private static final UUIDTraceIdGenerator instance = new UUIDTraceIdGenerator();

    @Override
    public String traceId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

}
