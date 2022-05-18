package com.alpha.coding.common.dubbo.filter;

import java.util.Map;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alpha.coding.bo.base.MapThreadLocalAdaptor;
import com.alpha.coding.bo.base.MapThreadLocalMirrorAspect;

import lombok.extern.slf4j.Slf4j;

/**
 * MapThreadLocalMirrorProviderFilter
 *
 * @version 1.0
 * Date: 2022/4/28
 */
@Slf4j
@Activate(group = Constants.PROVIDER, order = 2000)
public class MapThreadLocalMirrorProviderFilter implements Filter {

    private final MapThreadLocalMirrorAspect aspect = new MapThreadLocalMirrorAspect();

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        try {
            aspect.doBefore();
            Map<String, String> attachmentMap = RpcContext.getContext().getAttachments();
            if (attachmentMap != null) {
                attachmentMap.forEach(MapThreadLocalAdaptor::put);
            }
            return invoker.invoke(invocation);
        } finally {
            aspect.doAfter();
        }
    }

}
