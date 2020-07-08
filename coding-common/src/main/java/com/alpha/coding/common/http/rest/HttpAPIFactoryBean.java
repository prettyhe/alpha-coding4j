package com.alpha.coding.common.http.rest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.springframework.beans.factory.FactoryBean;

import lombok.Data;

/**
 * HttpAPIFactoryBean
 *
 * @version 1.0
 * Date: 2020/7/8
 */
@Data
public class HttpAPIFactoryBean<T> implements FactoryBean<T> {

    private Class<T> interfaceType;
    private InvocationHandler invocationHandler;

    @Override
    public T getObject() throws Exception {
        return (T) Proxy.newProxyInstance(interfaceType.getClassLoader(),
                new Class[] {interfaceType}, invocationHandler);
    }

    @Override
    public Class<T> getObjectType() {
        return interfaceType;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
