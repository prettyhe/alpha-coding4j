package com.alpha.coding.common.aop.invoke;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

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
import com.alpha.coding.common.utils.DateUtils;
import com.alpha.coding.common.utils.InvokeUtils;
import com.alpha.coding.common.utils.MD5Utils;
import com.alpha.coding.common.utils.StringUtils;

import lombok.Data;

/**
 * SyncInvokeAspect
 *
 * @version 1.0
 * Date: 2019年11月13日
 */
@Data
public class SyncInvokeAspect implements ApplicationContextAware {

    private static final String INVOKE_COUNT_TIME_RANGE_TODAY = "TODAY";

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
        if (evalCondition(joinPointContext.getJoinOperationContext(), syncInvoke.blockCondition())) {
            if (!syncInvoke.failCallback().equals(FailCallback.class)) {
                final FailCallback failCallback = FailCallbackFactory.instance(syncInvoke.failCallback());
                return failCallback.onBlocked(method, joinPoint.getArgs(),
                        BeanDefineUtils.resolveValue(applicationContext, syncInvoke.blockedText(), String.class));
            }
            return null;
        }
        if (!evalCondition(joinPointContext.getJoinOperationContext(), syncInvoke.condition())) {
            return joinPoint.proceed();
        }
        final String invokeKey = buildInvokeKey(joinPointContext.getJoinOperationContext(), syncInvoke.key());
        final RedisSync redisSync = syncInvoke.redisSync();
        if (redisSync.enable()) {
            final int maxInvokeTimes =
                    BeanDefineUtils.resolveValue(applicationContext, redisSync.maxInvokeTimes(), int.class);
            if (maxInvokeTimes == 0) {
                // 表示阻断调用
                if (!redisSync.failCallback().equals(FailCallback.class)) {
                    return FailCallbackFactory.instance(redisSync.failCallback())
                            .onExceedInvokeTimes(method, joinPoint.getArgs(), BeanDefineUtils
                                    .resolveValue(applicationContext, redisSync.exceedInvokeTimesText(), String.class));
                }
                return null;
            }
            RedisTemplate redisTemplate = (RedisTemplate) applicationContext.getBean(redisSync.redisTemplateBean());
            final int rateLimit = BeanDefineUtils.resolveValue(applicationContext, redisSync.rateLimit(), int.class);
            final long maxWaitSeconds =
                    BeanDefineUtils.resolveValue(applicationContext, redisSync.maxWaitSeconds(), long.class);
            final int tryLockIntervalMillis =
                    BeanDefineUtils.resolveValue(applicationContext, redisSync.tryLockIntervalMillis(), int.class);
            final Throwable[] throwableArray = new Throwable[1];
            Tuple<Boolean, Object> tuple = null;
            // 令牌阈值为1，使用分布式锁；令牌阈值大于1，使用限流
            if (rateLimit == 1 && redisSync.failFast()) {
                tuple = RedisTemplateUtils.doInLockAutoRenewalReturnOnLockFail(redisTemplate, invokeKey,
                        redisSync.expireSeconds(), maxWaitSeconds, tryLockIntervalMillis, () -> {
                            try {
                                return doWithInvokeTimesAspect(joinPoint, method, redisSync, redisTemplate,
                                        maxInvokeTimes);
                            } catch (Throwable throwable) {
                                throwableArray[0] = throwable;
                                return null;
                            }
                        });
            } else if (rateLimit == 1) {
                tuple = Tuple.of(true, RedisTemplateUtils.doInLockAutoRenewal(redisTemplate, invokeKey,
                        redisSync.expireSeconds(), tryLockIntervalMillis, () -> {
                            try {
                                return doWithInvokeTimesAspect(joinPoint, method, redisSync, redisTemplate,
                                        maxInvokeTimes);
                            } catch (Throwable throwable) {
                                throwableArray[0] = throwable;
                                return null;
                            }
                        }));
            } else if (rateLimit > 1 && redisSync.failFast()) {
                tuple = RedisTemplateUtils.doWithRateLimitAutoRenewalReturnOnLockFail(redisTemplate, invokeKey,
                        redisSync.expireSeconds(), rateLimit, () -> {
                            try {
                                return doWithInvokeTimesAspect(joinPoint, method, redisSync, redisTemplate,
                                        maxInvokeTimes);
                            } catch (Throwable throwable) {
                                throwableArray[0] = throwable;
                                return null;
                            }
                        });
            } else if (rateLimit > 1) {
                tuple = Tuple.of(true, RedisTemplateUtils.doWithRateLimitAutoRenewal(redisTemplate, invokeKey,
                        redisSync.expireSeconds(), rateLimit, tryLockIntervalMillis, () -> {
                            try {
                                return doWithInvokeTimesAspect(joinPoint, method, redisSync, redisTemplate,
                                        maxInvokeTimes);
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
        final int concurrency = BeanDefineUtils.resolveValue(applicationContext, syncInvoke.concurrency(), int.class);
        final InvokeUtils.InvokeResult invokeResult = InvokeUtils.syncInvoke(invokeLockCache, invokeKey, concurrency,
                syncInvoke.maxAwait() == -1 ? null : syncInvoke.maxAwait(), syncInvoke.failFastWhenAcquireFail(),
                syncInvoke.failFastWhenTimeout(), syncInvoke.failFastWhenWaitInterrupted(), joinPoint::proceed, null);
        if (invokeResult.isInterrupted() && syncInvoke.failFastWhenWaitInterrupted()
                && !syncInvoke.failCallback().equals(FailCallback.class)) {
            final FailCallback failCallback = FailCallbackFactory.instance(syncInvoke.failCallback());
            return failCallback.onLocalWaitInterrupted(method, joinPoint.getArgs(), invokeResult.getData(),
                    BeanDefineUtils.resolveValue(applicationContext, syncInvoke.failText(), String.class));
        }
        if (invokeResult.isWaitTimeout() && syncInvoke.failFastWhenTimeout()
                && !syncInvoke.failCallback().equals(FailCallback.class)) {
            final FailCallback failCallback = FailCallbackFactory.instance(syncInvoke.failCallback());
            return failCallback.onLocalWaitTimeout(method, joinPoint.getArgs(), invokeResult.getData(),
                    BeanDefineUtils.resolveValue(applicationContext, syncInvoke.failText(), String.class));
        }
        if (!invokeResult.isWinLock() && !syncInvoke.failCallback().equals(FailCallback.class)) {
            final FailCallback failCallback = FailCallbackFactory.instance(syncInvoke.failCallback());
            return failCallback.onLocalAcquireFail(method, joinPoint.getArgs(), invokeResult.getData(),
                    BeanDefineUtils.resolveValue(applicationContext, syncInvoke.failText(), String.class));
        }
        return invokeResult.getData();
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

    private String buildInvokeCountKey(JoinOperationContext context, String keyExpression) {
        if (StringUtils.isNotBlank(keyExpression)) {
            final Object key = AopHelper.evalSpELExpress(GlobalExpressionCache.getCache(), context.getMethodCacheKey(),
                    keyExpression, getExpressionParser(),
                    AopHelper.createEvaluationContext(context.getMetadata().getMethod(), context.getArgs(),
                            context.getTarget(), context.getMetadata().getTargetClass(), NO_RESULT,
                            applicationContext, getParameterNameDiscoverer()));
            return String.valueOf(key);
        } else {
            return String.valueOf(generateCount(context.getTarget(), context.getMetadata().getMethod(),
                    context.getMethodSignature()));
        }
    }

    /**
     * 执行条件表达式
     */
    private boolean evalCondition(JoinOperationContext context, String conditionExpression) {
        if (StringUtils.isBlank(conditionExpression) || "true".equalsIgnoreCase(conditionExpression)) {
            return true;
        } else if ("false".equalsIgnoreCase(conditionExpression)) {
            return false;
        }
        final Object condition = AopHelper.evalSpELExpress(GlobalExpressionCache.getCache(),
                context.getMethodCacheKey(), conditionExpression, getExpressionParser(),
                AopHelper.createEvaluationContext(context.getMetadata().getMethod(), context.getArgs(),
                        context.getTarget(), context.getMetadata().getTargetClass(), NO_RESULT,
                        applicationContext, getParameterNameDiscoverer()));
        return "true".equalsIgnoreCase(String.valueOf(condition));
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

    private Object generateCount(Object target, Method method, String methodSignature) {
        if (methodSignature == null) {
            return "_$INVOKE_NULL_COUNT_KEY$_";
        }
        String val = methodSignatureCache.get(methodSignature);
        if (val == null) {
            val = "_$INVOKE_COUNT_KEY$_" + MD5Utils.md5(methodSignature);
            methodSignatureCache.put(methodSignature, val);
        }
        return val;
    }

    /**
     * 检测掉用次数切面处理
     */
    private Object doWithInvokeTimesAspect(final ProceedingJoinPoint joinPoint, final Method method,
                                           RedisSync redisSync, final RedisTemplate redisTemplate,
                                           final int maxInvokeTimes) throws Throwable {
        // 需要检测调用次数
        if (maxInvokeTimes > 0) {
            final String invokeCountTimeRange =
                    BeanDefineUtils.resolveValue(applicationContext, redisSync.invokeCountTimeRange(), String.class);
            if (INVOKE_COUNT_TIME_RANGE_TODAY.equals(invokeCountTimeRange)
                    || StringUtils.isNumeric(invokeCountTimeRange)) {
                final JoinPointContext joinPointContext = new JoinPointContext(joinPoint);
                final String invokeCountKey =
                        buildInvokeCountKey(joinPointContext.getJoinOperationContext(), redisSync.invokeCountKey());
                final long incrbyUntilTh =
                        RedisTemplateUtils.incrbyUntilTh(redisTemplate, invokeCountKey, 1, maxInvokeTimes);
                if (incrbyUntilTh <= 0) {
                    // 已达到最大次数
                    if (!redisSync.failCallback().equals(FailCallback.class)) {
                        return FailCallbackFactory.instance(redisSync.failCallback())
                                .onExceedInvokeTimes(method, joinPoint.getArgs(), BeanDefineUtils
                                        .resolveValue(applicationContext, redisSync.exceedInvokeTimesText(),
                                                String.class));
                    }
                    return null;
                }
                // 设置时间区间有效期
                if (incrbyUntilTh <= 10) {
                    if (INVOKE_COUNT_TIME_RANGE_TODAY.equals(invokeCountTimeRange)) {
                        redisTemplate.expire(invokeCountKey, DateUtils.getDayRemainSeconds(), TimeUnit.SECONDS);
                    } else {
                        redisTemplate.expire(invokeCountKey, new BigDecimal(invokeCountTimeRange).longValue(),
                                TimeUnit.SECONDS);
                    }
                }
            }
        }
        return joinPoint.proceed();
    }

}
