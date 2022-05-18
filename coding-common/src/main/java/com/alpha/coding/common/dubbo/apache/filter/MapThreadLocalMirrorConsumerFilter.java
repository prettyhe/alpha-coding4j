package com.alpha.coding.common.dubbo.apache.filter;

import java.util.Set;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;

import com.alpha.coding.bo.base.MapThreadLocalAdaptor;

import lombok.extern.slf4j.Slf4j;

/**
 * MapThreadLocalMirrorConsumerFilter
 *
 * @version 1.0
 * Date: 2022/4/28
 */
@Slf4j
@Activate(group = CommonConstants.CONSUMER, order = 2000)
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
