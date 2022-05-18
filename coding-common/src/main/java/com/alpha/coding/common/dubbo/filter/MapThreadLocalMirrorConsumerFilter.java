package com.alpha.coding.common.dubbo.filter;

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

import lombok.extern.slf4j.Slf4j;

/**
 * MapThreadLocalMirrorConsumerFilter
 *
 * @version 1.0
 * Date: 2022/4/28
 */
@Slf4j
@Activate(group = Constants.CONSUMER)
public class MapThreadLocalMirrorConsumerFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        final Set<String> keys = MapThreadLocalAdaptor.getKeys();
        if (keys != null) {
            for (String key : keys) {
                final Object val = MapThreadLocalAdaptor.get(key);
                if (val != null) {
                    RpcContext.getContext().setAttachment(key, String.valueOf(val));
                }
            }
        }
        return invoker.invoke(invocation);
    }

}
