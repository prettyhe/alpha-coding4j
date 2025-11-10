package com.alpha.coding.common.dubbo.filter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alpha.coding.bo.base.MapThreadLocalAdaptor;
import com.alpha.coding.common.dubbo.DubboContextTool;

import lombok.extern.slf4j.Slf4j;

/**
 * MapThreadLocalMirrorConsumerFilter
 *
 * @version 1.0
 * Date: 2022/4/28
 */
@Slf4j
@Activate(group = Constants.CONSUMER, order = 2000)
public class MapThreadLocalMirrorConsumerFilter implements Filter {

    private static final List<String> IGNORE_KEYS =
            Arrays.asList("path", "group", "version", "dubbo.version", "interface", "dubbo",
                    "token", "timeout", "timeout.attached", "async", "tag", "force.use.tag",
                    "generic", "generic.attachment", "remote.application", "trace.id", "span.id",
                    "side", "monitor", "input", "output", "timestamp", "start.time",
                    "serialization", "codec", "retries", "loadbalance", "cluster",
                    "x-trace-id", "x-span-id", "x-parent-id");

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if (DubboContextTool.useApacheDubbo()) {
            return invoker.invoke(invocation);
        }
        final Set<String> keys = MapThreadLocalAdaptor.getKeys();
        if (keys != null) {
            for (String key : keys) {
                if (IGNORE_KEYS.contains(key)) {
                    continue;
                }
                final Object val = MapThreadLocalAdaptor.get(key);
                if (val != null) {
                    final Map<String, String> attachments = invocation.getAttachments();
                    if (attachments != null) {
                        attachments.put(key, String.valueOf(val));
                    } else {
                        RpcContext.getContext().setAttachment(key, String.valueOf(val));
                    }
                }
            }
        }
        return invoker.invoke(invocation);
    }

}
