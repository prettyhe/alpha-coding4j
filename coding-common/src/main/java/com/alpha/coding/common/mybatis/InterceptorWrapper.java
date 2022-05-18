package com.alpha.coding.common.mybatis;

import java.util.Properties;
import java.util.function.Supplier;

import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Invocation;

/**
 * InterceptorWrapper
 *
 * @version 1.0
 * Date: 2022/4/27
 */
public class InterceptorWrapper implements Interceptor {

    private final Supplier<Interceptor> interceptorProvider;

    public InterceptorWrapper(Supplier<Interceptor> interceptorProvider) {
        this.interceptorProvider = interceptorProvider;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        return rawInterceptor().intercept(invocation);
    }

    @Override
    public Object plugin(Object target) {
        return rawInterceptor().plugin(target);
    }

    @Override
    public void setProperties(Properties properties) {
        rawInterceptor().setProperties(properties);
    }

    public Interceptor rawInterceptor() {
        return interceptorProvider.get();
    }

}
