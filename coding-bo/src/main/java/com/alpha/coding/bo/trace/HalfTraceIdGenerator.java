package com.alpha.coding.bo.trace;

import javax.validation.constraints.NotNull;

import lombok.extern.slf4j.Slf4j;

/**
 * HalfTraceIdGenerator
 *
 * @version 1.0
 * Date: 2019-12-27
 */
@Slf4j
public class HalfTraceIdGenerator implements TraceIdGenerator {

    private TraceIdGenerator traceIdGenerator;

    public HalfTraceIdGenerator(@NotNull TraceIdGenerator traceIdGenerator) {
        this.traceIdGenerator = traceIdGenerator;
    }

    @Override
    public String traceId() {
        return traceIdGenerator.traceId();
    }

    public String halfTrace(String traceId) {
        if (traceId == null) {
            return traceIdGenerator.traceId();
        }
        if (traceId.length() < 2) {
            return traceId;
        }
        final int half = traceId.length() / 2;
        int complementLength = traceId.length() - 1 - half;
        StringBuilder sb = new StringBuilder();
        while (sb.length() < complementLength) {
            sb.append(traceIdGenerator.traceId());
        }
        return traceId.substring(0, half) + "-" + sb.substring(0, complementLength);
    }

}
