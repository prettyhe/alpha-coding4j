package com.alpha.coding.common.aop.assist;

import java.lang.reflect.Method;

import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.util.ObjectUtils;

import lombok.Getter;

/**
 * JoinOperationContext
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Getter
public class JoinOperationContext {

    private final JoinOperationMetadata metadata;
    private final Object[] args;
    private final Object target;
    private final AnnotatedElementKey methodCacheKey;
    private final String methodSignature;

    public JoinOperationContext(JoinOperationMetadata metadata, Object[] args, Object target) {
        this.metadata = metadata;
        this.args = extractArgs(metadata.getMethod(), args);
        this.target = target;
        this.methodCacheKey = new AnnotatedElementKey(metadata.getMethod(), metadata.getTargetClass());
        this.methodSignature = metadata.getSignature().toLongString();
    }

    private Object[] extractArgs(Method method, Object[] args) {
        if (!method.isVarArgs()) {
            return args;
        }
        Object[] varArgs = ObjectUtils.toObjectArray(args[args.length - 1]);
        Object[] combinedArgs = new Object[args.length - 1 + varArgs.length];
        System.arraycopy(args, 0, combinedArgs, 0, args.length - 1);
        System.arraycopy(varArgs, 0, combinedArgs, args.length - 1, varArgs.length);
        return combinedArgs;
    }

}
