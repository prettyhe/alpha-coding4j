package com.alpha.coding.common.dubbo.filter;

import java.util.Map;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alpha.coding.bo.base.MapThreadLocalAdaptor;
import com.alpha.coding.bo.base.MapThreadLocalMirrorAspect;
import com.alpha.coding.common.dubbo.DubboContextTool;

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
        if (DubboContextTool.useApacheDubbo()) {
            return invoker.invoke(invocation);
        }
        try {
            aspect.doBefore();
            Map<String, String> attachmentMap = invocation.getAttachments();
            if (attachmentMap != null) {
                // 只覆盖那些未在MapThreadLocalAdaptor中定义的，避免覆盖了其它filter中修改过的值
                attachmentMap.entrySet().stream().filter(en -> !MapThreadLocalAdaptor.containsKey(en.getKey()))
                        .forEach(en -> MapThreadLocalAdaptor.put(en.getKey(), en.getValue()));
            }
            return invoker.invoke(invocation);
        } finally {
            aspect.doAfter();
        }
    }

}
