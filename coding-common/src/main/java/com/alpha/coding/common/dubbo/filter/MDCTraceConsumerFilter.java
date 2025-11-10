package com.alpha.coding.common.dubbo.filter;

import java.util.Map;

import org.slf4j.MDC;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alpha.coding.bo.constant.Keys;
import com.alpha.coding.bo.trace.TimestampBase62UUIDTraceIdGenerator;
import com.alpha.coding.bo.trace.TraceIdGenerator;
import com.alpha.coding.common.dubbo.DubboContextTool;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * MDCTraceConsumerFilter
 *
 * @version 1.0
 * Date: 2019年10月28日
 */
@Slf4j
@Activate(group = Constants.CONSUMER, order = -201)
public class MDCTraceConsumerFilter implements Filter {

    private static final String metaTraceId = "_trId_";

    @Setter
    private TraceIdGenerator traceIdGenerator = TimestampBase62UUIDTraceIdGenerator.getInstance();

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if (DubboContextTool.useApacheDubbo()) {
            return invoker.invoke(invocation);
        }
        final Map<String, String> attachments = invocation.getAttachments();
        if (attachments == null || MDC.getMDCAdapter() == null || attachments.get(metaTraceId) != null) {
            return invoker.invoke(invocation);
        }
        String traceId = MDC.get(Keys.TRACE_ID);
        if (traceId == null) {
            traceId = traceIdGenerator.traceId();
        }
        attachments.put(metaTraceId, traceId);
        return invoker.invoke(invocation);
    }

}
