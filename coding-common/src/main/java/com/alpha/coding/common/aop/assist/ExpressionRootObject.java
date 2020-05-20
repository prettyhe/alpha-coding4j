package com.alpha.coding.common.aop.assist;

import java.lang.reflect.Method;

import org.springframework.util.Assert;

import lombok.Getter;

/**
 * ExpressionRootObject
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Getter
public class ExpressionRootObject {

    private final Method method;
    private final Object[] args;
    private final Object target;
    private final Class<?> targetClass;

    public ExpressionRootObject(Method method, Object[] args, Object target, Class<?> targetClass) {
        Assert.notNull(method, "Method is required");
        Assert.notNull(targetClass, "targetClass is required");
        this.method = method;
        this.target = target;
        this.targetClass = targetClass;
        this.args = args;
    }

}
