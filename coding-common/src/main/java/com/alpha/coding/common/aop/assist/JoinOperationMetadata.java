package com.alpha.coding.common.aop.assist;

import java.lang.reflect.Method;

import org.aspectj.lang.reflect.MethodSignature;

import lombok.Getter;

/**
 * JoinOperationMetadata
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Getter
public class JoinOperationMetadata {

    private final Method method;
    private final Class<?> targetClass;
    private final MethodSignature signature;

    public JoinOperationMetadata(Method method, Class<?> targetClass, MethodSignature signature) {
        this.method = method;
        this.targetClass = targetClass;
        this.signature = signature;
    }

}
