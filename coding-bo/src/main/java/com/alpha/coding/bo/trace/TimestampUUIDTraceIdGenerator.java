package com.alpha.coding.bo.trace;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import lombok.Getter;

/**
 * TimestampUUIDTraceIdGenerator
 *
 * @version 1.0
 * Date: 2020/5/11
 */
public class TimestampUUIDTraceIdGenerator implements TraceIdGenerator {

    private static final int LENGTH = 20;

    @Getter
    private static final TimestampUUIDTraceIdGenerator instance = new TimestampUUIDTraceIdGenerator();

    @Override
    public String traceId() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        final int start = ThreadLocalRandom.current().nextInt(0, uuid.length() - (LENGTH - timestamp.length()));
        return timestamp + uuid.substring(start, start + (LENGTH - timestamp.length()));
    }

}
