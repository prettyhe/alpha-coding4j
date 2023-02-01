package com.alpha.coding.common.aop.assist;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.AnnotatedElementKey;

import lombok.Getter;

/**
 * ProceedingJoinPointContext
 *
 * @version 1.0
 * Date: 2023/1/31
 */
@Getter
public class JoinPointContext {

    private static final Map<AnnotatedElementKey, JoinOperationMetadata> METADATA_CACHE = new ConcurrentHashMap<>(1024);
    @Getter
    private static final Object NO_RESULT = new Object();

    private final MethodSignature methodSignature;
    private final Class<?> targetClass;
    private final AnnotatedElementKey metadataCacheKey;
    private final JoinOperationMetadata joinOperationMetadata;
    private final JoinOperationContext joinOperationContext;

    public JoinPointContext(final JoinPoint joinPoint) {
        this.methodSignature = (MethodSignature) joinPoint.getSignature();
        this.targetClass = AopHelper.getTargetClass(joinPoint.getTarget());
        this.metadataCacheKey = new AnnotatedElementKey(this.methodSignature.getMethod(), this.targetClass);
        this.joinOperationMetadata = METADATA_CACHE.computeIfAbsent(this.metadataCacheKey,
                k -> new JoinOperationMetadata(this.methodSignature.getMethod(), this.targetClass,
                        this.methodSignature));
        this.joinOperationContext = new JoinOperationContext(this.joinOperationMetadata,
                joinPoint.getArgs(), joinPoint.getTarget());
    }

    public Method getMethod() {
        return joinOperationMetadata.getMethod();
    }

    public Class<?> getTargetClass() {
        return joinOperationMetadata.getTargetClass();
    }

    public Object[] getArgs() {
        return joinOperationContext.getArgs();
    }

    public Object getTarget() {
        return joinOperationContext.getTarget();
    }

}
