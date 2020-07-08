package com.alpha.coding.common.bean.comm;

import org.springframework.beans.factory.FactoryBean;

import lombok.Data;

/**
 * CustomFactoryBean
 *
 * @version 1.0
 * Date: 2020/6/11
 */
@Data
public class CustomFactoryBean<T> implements FactoryBean<T> {

    private Class<T> type;
    private T target;

    public CustomFactoryBean() {
    }

    public CustomFactoryBean(Class<T> type, T target) {
        this.type = type;
        this.target = target;
    }

    @Override
    public T getObject() throws Exception {
        return target;
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
