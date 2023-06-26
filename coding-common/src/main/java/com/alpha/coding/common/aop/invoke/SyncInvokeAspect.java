package com.alpha.coding.common.aop.invoke;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.expression.ExpressionParser;

import com.alpha.coding.bo.base.Tuple;
import com.alpha.coding.common.aop.assist.AopHelper;
import com.alpha.coding.common.aop.assist.JoinOperationContext;
import com.alpha.coding.common.aop.assist.JoinPointContext;
import com.alpha.coding.common.aop.assist.SpelExpressionParserFactory;
import com.alpha.coding.common.bean.register.BeanDefineUtils;
import com.alpha.coding.common.redis.RedisTemplateUtils;
import com.alpha.coding.common.spring.spel.GlobalExpressionCache;
import com.alpha.coding.common.utils.InvokeUtils;
import com.alpha.coding.common.utils.MD5Utils;
import com.alpha.coding.common.utils.StringUtils;

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
    private ExpressionParser expressionParser = SpelExpressionParserFactory.getDefaultParser();
    private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    private final Object NO_RESULT = new Object();
    private final Map<String, String> methodSignatureCache = new ConcurrentHashMap<>(64);
    private final Map<String, InvokeUtils.InvokeLock> invokeLockCache = new ConcurrentHashMap<>(256);

    /**
     * 切面逻辑
     */
    @SuppressWarnings({"rawtypes"})
    public Object doAspect(final ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        final Class<?> targetClass = AopHelper.getTargetClass(joinPoint.getTarget());
        final Method method = AopHelper.getTargetMethod(targetClass, signature.getMethod());
        if (!method.isAnnotationPresent(SyncInvoke.class)) {
            return joinPoint.proceed();
        }
        final SyncInvoke syncInvoke = method.getAnnotation(SyncInvoke.class);
        final JoinPointContext joinPointContext = new JoinPointContext(joinPoint);
        final String invokeKey = buildInvokeKey(joinPointContext.getJoinOperationContext(), syncInvoke.key());
        final RedisSync redisSync = syncInvoke.redisSync();
        if (redisSync.enable()) {
            RedisTemplate redisTemplate = (RedisTemplate) applicationContext.getBean(redisSync.redisTemplateBean());
            final int rateLimit = BeanDefineUtils.resolveValue(applicationContext, redisSync.rateLimit(), int.class);
            final Throwable[] throwableArray = new Throwable[1];
            Tuple<Boolean, Object> tuple = null;
            // 令牌阈值为1，使用分布式锁；令牌阈值大于1，使用限流
            if (rateLimit == 1 && redisSync.failFast()) {
                tuple = RedisTemplateUtils.doInLockAutoRenewalReturnOnLockFail(redisTemplate, invokeKey,
                        redisSync.expireSeconds(), () -> {
                            try {
                                return joinPoint.proceed();
                            } catch (Throwable throwable) {
                                throwableArray[0] = throwable;
                                return null;
                            }
                        });
            } else if (rateLimit == 1) {
                tuple = Tuple.of(true, RedisTemplateUtils.doInLockAutoRenewal(redisTemplate, invokeKey,
                        redisSync.expireSeconds(), null, () -> {
                            try {
                                return joinPoint.proceed();
                            } catch (Throwable throwable) {
                                throwableArray[0] = throwable;
                                return null;
                            }
                        }));
            } else if (rateLimit > 1 && redisSync.failFast()) {
                tuple = RedisTemplateUtils.doWithRateLimitAutoRenewalReturnOnLockFail(redisTemplate, invokeKey,
                        redisSync.expireSeconds(), rateLimit, () -> {
                            try {
                                return joinPoint.proceed();
                            } catch (Throwable throwable) {
                                throwableArray[0] = throwable;
                                return null;
                            }
                        });
            } else if (rateLimit > 1) {
                tuple = Tuple.of(true, RedisTemplateUtils.doWithRateLimitAutoRenewal(redisTemplate, invokeKey,
                        redisSync.expireSeconds(), rateLimit, null, () -> {
                            try {
                                return joinPoint.proceed();
                            } catch (Throwable throwable) {
                                throwableArray[0] = throwable;
                                return null;
                            }
                        }));
            } else {
                throw new IllegalArgumentException("rateLimit must positive number!");
            }
            if (throwableArray[0] != null) {
                throw throwableArray[0];
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
            final Object key = AopHelper.evalSpELExpress(GlobalExpressionCache.getCache(), context.getMethodCacheKey(),
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
