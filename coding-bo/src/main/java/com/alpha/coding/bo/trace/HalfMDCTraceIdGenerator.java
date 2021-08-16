package com.alpha.coding.bo.trace;

import org.slf4j.MDC;

import com.alpha.coding.bo.constant.Keys;

import lombok.Getter;

/**
 * HalfMDCTraceIdGenerator
 *
 * @version 1.0
 * Date: 2021/8/16
 */
public class HalfMDCTraceIdGenerator extends HalfTraceIdGenerator {

    @Getter
    private static final HalfMDCTraceIdGenerator timestampBase62Instance =
            new HalfMDCTraceIdGenerator(new TimestampBase62UUIDTraceIdGenerator());
    @Getter
    private static final HalfMDCTraceIdGenerator uuidInstance =
            new HalfMDCTraceIdGenerator(new UUIDTraceIdGenerator());

    public HalfMDCTraceIdGenerator(TraceIdGenerator traceIdGenerator) {
        super(traceIdGenerator);
    }

    @Override
    public String traceId() {
        if (MDC.getMDCAdapter() == null) {
            return super.traceId();
        }
        return halfTrace(MDC.get(Keys.TRACE_ID));
    }

}
