package com.alpha.coding.common.utils;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.slf4j.MDC;

import com.alpha.coding.bo.base.MapThreadLocalAdaptor;
import com.alpha.coding.bo.constant.Keys;

/**
 * TraceIdUtils
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class TraceIdUtils {

    public static String getTraceId() {
        if (MDC.getMDCAdapter() == null) {
            return (String) MapThreadLocalAdaptor.get(Keys.TRACE_ID);
        }
        final String traceId = MDC.get(Keys.TRACE_ID);
        if (traceId != null) {
            return traceId;
        }
        return (String) MapThreadLocalAdaptor.get(Keys.TRACE_ID);
    }

    public static String getTraceIdIfAbsent() {
        String traceId = getTraceId();
        if (traceId == null) {
            traceId = UUID.randomUUID().toString().replaceAll("-", "");
            putTraceId(traceId);
        }
        return traceId;
    }

    public static void putTraceId(@NotNull String traceId) {
        if (traceId != null) {
            if (MDC.getMDCAdapter() != null) {
                MDC.put(Keys.TRACE_ID, traceId);
            }
            MapThreadLocalAdaptor.put(Keys.TRACE_ID, traceId);
        }
    }

}
