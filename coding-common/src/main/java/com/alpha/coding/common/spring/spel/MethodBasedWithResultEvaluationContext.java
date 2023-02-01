package com.alpha.coding.common.spring.spel;

import java.lang.reflect.Method;

import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.ParameterNameDiscoverer;

/**
 * MethodBasedWithResultEvaluationContext
 *
 * @version 1.0
 * Date: 2023/1/31
 */
public class MethodBasedWithResultEvaluationContext extends MethodBasedEvaluationContext {

    /**
     * 表达式中取方法返回值的变量名称
     */
    private static final String RETURN_OBJ_NAME = "returnObj";

    /**
     * 方法返回值
     */
    private final Object result;

    public MethodBasedWithResultEvaluationContext(Object rootObject, Method method, Object[] arguments,
                                                  ParameterNameDiscoverer parameterNameDiscoverer, Object result) {
        super(rootObject, method, arguments, parameterNameDiscoverer);
        this.result = result;
    }

    /**
     * Load the param information only when needed.
     */
    @Override
    public Object lookupVariable(String name) {
        final Object variable = super.lookupVariable(name);
        if (variable == null && RETURN_OBJ_NAME.equals(name)) {
            return result;
        }
        return variable;
    }

}
