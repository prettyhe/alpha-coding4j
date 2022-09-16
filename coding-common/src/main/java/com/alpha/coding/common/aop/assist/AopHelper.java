package com.alpha.coding.common.aop.assist;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;

import lombok.extern.slf4j.Slf4j;

/**
 * AopHelper
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class AopHelper {

    private static final Map<AnnotatedElementKey, Method> TARGET_METHOD_CACHE = new ConcurrentHashMap<>(64);

    public static Class<?> getTargetClass(Object target) {
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(target);
        if (targetClass == null) {
            targetClass = target.getClass();
        }
        return targetClass;
    }

    public static Class<?> getProxyTargetClass(Object proxy) {
        try {
            return getTarget(proxy).getClass();
        } catch (Exception e) {
            log.warn("getProxyTargetClass error: {}", e.getMessage());
            return proxy.getClass();
        }
    }

    public static Method getTargetMethod(Class<?> targetClass, Method method) {
        AnnotatedElementKey methodKey = new AnnotatedElementKey(method, targetClass);
        Method targetMethod = TARGET_METHOD_CACHE.get(methodKey);
        if (targetMethod == null) {
            targetMethod = AopUtils.getMostSpecificMethod(method, targetClass);
            if (targetMethod == null) {
                targetMethod = method;
            }
            TARGET_METHOD_CACHE.put(methodKey, targetMethod);
        }
        return targetMethod;
    }

    public static Object evalSpELExpress(Map<ExpressionKey, Expression> cache, AnnotatedElementKey methodKey,
                                         String keyExpression, ExpressionParser expressionParser,
                                         EvaluationContext evalContext) {
        return getExpression(cache, methodKey, keyExpression, expressionParser).getValue(evalContext);
    }

    /**
     * Return the {@link Expression} for the specified SpEL value
     * <p>Parse the expression if it hasn't been already.
     *
     * @param cache            the cache to use
     * @param elementKey       the element on which the expression is defined
     * @param expression       the expression to parse
     * @param expressionParser the expressionParser
     */
    public static Expression getExpression(Map<ExpressionKey, Expression> cache, AnnotatedElementKey elementKey,
                                           String expression, ExpressionParser expressionParser) {
        ExpressionKey expressionKey = createKey(elementKey, expression);
        Expression expr = cache.get(expressionKey);
        if (expr == null) {
            expr = expressionParser.parseExpression(expression);
            cache.put(expressionKey, expr);
        }
        return expr;
    }

    private static ExpressionKey createKey(AnnotatedElementKey elementKey, String expression) {
        return new ExpressionKey(elementKey, expression);
    }

    /**
     * Create an {@link EvaluationContext}.
     *
     * @param method                  the method
     * @param args                    the method arguments
     * @param target                  the target object
     * @param targetClass             the target class
     * @param result                  the return value
     * @param beanFactory             the beanFactory
     * @param parameterNameDiscoverer the parameterNameDiscoverer
     * @return the evaluation context
     */
    public static EvaluationContext createEvaluationContext(Method method, Object[] args, Object target,
                                                            Class<?> targetClass, Object result,
                                                            BeanFactory beanFactory,
                                                            ParameterNameDiscoverer parameterNameDiscoverer) {
        ExpressionRootObject rootObject = new ExpressionRootObject(method, args, target, targetClass);
        Method targetMethod = getTargetMethod(targetClass, method);
        MethodBasedEvaluationContext evaluationContext = new MethodBasedEvaluationContext(
                rootObject, targetMethod, args, parameterNameDiscoverer);
        if (beanFactory != null) {
            evaluationContext.setBeanResolver(new BeanFactoryResolver(beanFactory));
        }
        return evaluationContext;
    }

    /**
     * 获取 目标对象
     *
     * @param proxy 代理对象
     * @return 目标对象
     * @throws Exception 异常
     */
    public static Object getTarget(Object proxy) throws Exception {
        if (!AopUtils.isAopProxy(proxy)) {
            return proxy;
        }
        if (AopUtils.isJdkDynamicProxy(proxy)) {
            return getJdkDynamicProxyTargetObject(proxy);
        } else if (AopUtils.isCglibProxy(proxy)) {
            return getCglibProxyTargetObject(proxy);
        } else {
            throw new UnsupportedOperationException("unknown proxy");
        }
    }

    private static Object getCglibProxyTargetObject(Object proxy) throws Exception {
        Field h = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");
        h.setAccessible(true);
        Object dynamicAdvisedInterceptor = h.get(proxy);
        Field advised = dynamicAdvisedInterceptor.getClass().getDeclaredField("advised");
        advised.setAccessible(true);
        return ((AdvisedSupport) advised.get(dynamicAdvisedInterceptor)).getTargetSource().getTarget();
    }

    private static Object getJdkDynamicProxyTargetObject(Object proxy) throws Exception {
        Field h = proxy.getClass().getSuperclass().getDeclaredField("h");
        h.setAccessible(true);
        AopProxy aopProxy = (AopProxy) h.get(proxy);
        Field advised = aopProxy.getClass().getDeclaredField("advised");
        advised.setAccessible(true);
        return ((AdvisedSupport) advised.get(aopProxy)).getTargetSource().getTarget();
    }

}
