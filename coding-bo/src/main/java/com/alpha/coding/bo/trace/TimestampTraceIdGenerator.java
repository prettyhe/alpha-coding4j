package com.alpha.coding.bo.trace;

import java.util.UUID;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * TimestampTraceIdGenerator
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class TimestampTraceIdGenerator implements TraceIdGenerator {

    @Getter
    private static final TraceIdGenerator instance = new TimestampTraceIdGenerator();

    @Override
    public String traceId() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        return timestamp + uuid.substring(timestamp.length());
    }
}
