package com.alpha.coding.common.dubbo.apache.filter;

import java.util.Map;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;

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
@Activate(group = CommonConstants.PROVIDER, order = 2000)
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
