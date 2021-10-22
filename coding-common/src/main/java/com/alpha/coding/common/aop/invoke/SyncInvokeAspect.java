package com.alpha.coding.common.aop.invoke;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;

import com.alpha.coding.bo.base.Tuple;
import com.alpha.coding.common.aop.assist.AopHelper;
import com.alpha.coding.common.aop.assist.ExpressionKey;
import com.alpha.coding.common.aop.assist.JoinOperationContext;
import com.alpha.coding.common.aop.assist.JoinOperationMetadata;
import com.alpha.coding.common.aop.assist.SpelExpressionParserFactory;
import com.alpha.coding.common.bean.register.BeanDefineUtils;
import com.alpha.coding.common.redis.RedisTemplateUtils;
import com.alpha.coding.common.utils.InvokeUtils;
import com.alpha.coding.common.utils.MD5Utils;

import lombok.Data;

/**
 * SyncInvokeAspect
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Data
public class SyncInvokeAspect implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    private ExpressionParser expressionParser = new SpelExpressionParserFactory().getInstance();
    private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    private final Object NO_RESULT = new Object();
    private final Map<AnnotatedElementKey, JoinOperationMetadata> metadataCache = new ConcurrentHashMap<>(1024);
    private final Map<ExpressionKey, Expression> expressionCache = new ConcurrentHashMap<>(64);
    private final Map<String, String> methodSignatureCache = new ConcurrentHashMap<>(64);
    private final Map<String, InvokeUtils.InvokeLock> invokeLockCache = new ConcurrentHashMap<>(256);

    /**
     * 切面逻辑
     */
    public Object doAspect(final ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        final Class<?> targetClass = AopHelper.getTargetClass(joinPoint.getTarget());
        final Method method = AopHelper.getTargetMethod(targetClass, signature.getMethod());
        if (!method.isAnnotationPresent(SyncInvoke.class)) {
            return joinPoint.proceed();
        }
        final SyncInvoke syncInvoke = method.getAnnotation(SyncInvoke.class);
        AnnotatedElementKey metadataCacheKey = new AnnotatedElementKey(signature.getMethod(), targetClass);
        JoinOperationMetadata metadata = metadataCache.get(metadataCacheKey);
        if (metadata == null) {
            metadata = new JoinOperationMetadata(signature.getMethod(), targetClass, signature);
            metadataCache.put(metadataCacheKey, metadata);
        }
        JoinOperationContext operationContext = new JoinOperationContext(metadata,
                joinPoint.getArgs(), joinPoint.getTarget());
        final String invokeKey = buildInvokeKey(operationContext, syncInvoke.key());
        final RedisSync redisSync = syncInvoke.redisSync();
        if (redisSync.enable()) {
            RedisTemplate redisTemplate = (RedisTemplate) applicationContext.getBean(redisSync.redisTemplateBean());
            Throwable[] throwables = new Throwable[1];
            Tuple<Boolean, Object> tuple = redisSync.failFast() ? RedisTemplateUtils
                    .doInLockAutoRenewalReturnOnLockFail(redisTemplate, invokeKey,
                            redisSync.expireSeconds(), () -> {
                                try {
                                    return joinPoint.proceed();
                                } catch (Throwable throwable) {
                                    throwables[0] = throwable;
                                    return null;
                                }
                            }) : Tuple.of(true, RedisTemplateUtils
                    .doInLockAutoRenewal(redisTemplate, invokeKey,
                            redisSync.expireSeconds(), null, () -> {
                                try {
                                    return joinPoint.proceed();
                                } catch (Throwable throwable) {
                                    throwables[0] = throwable;
                                    return null;
                                }
                            }));
            if (throwables[0] != null) {
                throw throwables[0];
            } else {
                if (!tuple.getF() && !redisSync.failCallback().equals(FailCallback.class)) {
                    return FailCallbackFactory.instance(redisSync.failCallback())
                            .callback(method, joinPoint.getArgs(), BeanDefineUtils
                                    .resolveValue(applicationContext, redisSync.failText(), String.class));
                }
                return tuple.getS();
            }
        }
        return InvokeUtils.syncInvoke(invokeLockCache, invokeKey,
                syncInvoke.maxAwait() == -1 ? null : syncInvoke.maxAwait(),
                joinPoint::proceed, null).getData();
    }

    private String buildInvokeKey(JoinOperationContext context, String keyExpression) {
        if (StringUtils.isNotBlank(keyExpression)) {
            final Object key = AopHelper.evalSpELExpress(expressionCache, context.getMethodCacheKey(),
                    keyExpression, getExpressionParser(),
                    AopHelper.createEvaluationContext(context.getMetadata().getMethod(), context.getArgs(),
                            context.getTarget(), context.getMetadata().getTargetClass(), NO_RESULT,
                            applicationContext, getParameterNameDiscoverer()));
            return String.valueOf(key);
        } else {
            return String.valueOf(generate(context.getTarget(), context.getMetadata().getMethod(),
                    context.getMethodSignature()));
        }
    }

    private Object generate(Object target, Method method, String methodSignature) {
        if (methodSignature == null) {
            return "_$INVOKE_NULL_KEY$_";
        }
        String val = methodSignatureCache.get(methodSignature);
        if (val == null) {
            val = "_$INVOKE_KEY$_" + MD5Utils.md5(methodSignature);
            methodSignatureCache.put(methodSignature, val);
        }
        return val;
    }

}
