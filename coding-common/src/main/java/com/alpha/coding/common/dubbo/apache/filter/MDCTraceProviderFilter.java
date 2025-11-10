package com.alpha.coding.common.dubbo.apache.filter;

import java.util.Map;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;
import org.slf4j.MDC;

import com.alpha.coding.bo.constant.Keys;
import com.alpha.coding.bo.trace.TimestampBase62UUIDTraceIdGenerator;
import com.alpha.coding.bo.trace.TraceIdGenerator;
import com.alpha.coding.bo.trace.UUIDTraceIdGenerator;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * MDCTraceProviderFilter
 *
 * @version 1.0
 * Date: 2019年10月28日
 */
@Slf4j
@Activate(group = CommonConstants.PROVIDER, order = -200)
public class MDCTraceProviderFilter implements Filter {

    private static final String metaTraceId = "_trId_";
    @Setter
    private TraceIdGenerator traceIdGenerator = TimestampBase62UUIDTraceIdGenerator.getInstance();
    @Setter
    private TraceIdGenerator tailTraceIdGenerator = UUIDTraceIdGenerator.getInstance();

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        Map<String, String> attaches = invocation.getAttachments();
        if (attaches == null || attaches.get(metaTraceId) == null) {
            return invoker.invoke(invocation);
        }
        if (MDC.getMDCAdapter() != null) {
            if (attaches.get(metaTraceId) == null) {
                final String traceId = traceIdGenerator.traceId();
                MDC.put(Keys.TRACE_ID, traceId);
                if (log.isDebugEnabled()) {
                    log.debug("NewTraceId generate: {}", traceId);
                }
            } else {
                final String traceId = attaches.get(metaTraceId);
                if (traceId.length() < 2) {
                    MDC.put(Keys.TRACE_ID, traceId);
                } else {
                    final int half = traceId.length() / 2;
                    int complementLength = traceId.length() - 1 - half;
                    StringBuilder sb = new StringBuilder();
                    while (sb.length() < complementLength) {
                        sb.append(tailTraceIdGenerator.traceId());
                    }
                    String newTraceId = traceId.substring(0, half) + "-" + sb.substring(0, complementLength);
                    MDC.put(Keys.TRACE_ID, newTraceId);
                    if (log.isDebugEnabled()) {
                        log.debug("NewTraceId derive: {} ==> {}", traceId, newTraceId);
                    }
                }
            }
        }
        try {
            return invoker.invoke(invocation);
        } finally {
            if (MDC.getMDCAdapter() != null) {
                MDC.remove(Keys.TRACE_ID);
            }
        }
    }

}
