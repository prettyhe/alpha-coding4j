package com.alpha.coding.common.bean.comm;

import java.util.function.Supplier;

import org.springframework.beans.factory.FactoryBean;

import lombok.Data;

/**
 * CustomFactoryBeanProvider
 *
 * @version 1.0
 * Date: 2022/4/27
 */
@Data
public class CustomFactoryBeanProvider<T> implements FactoryBean<T> {

    private Class<T> type;
    private Supplier<T> targetProvider;

    public CustomFactoryBeanProvider() {
    }

    public CustomFactoryBeanProvider(Class<T> type, Supplier<T> targetProvider) {
        this.type = type;
        this.targetProvider = targetProvider;
    }

    @Override
    public T getObject() throws Exception {
        return targetProvider.get();
    }

    @Override
    public Class<?> getObjectType() {
        return type;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
