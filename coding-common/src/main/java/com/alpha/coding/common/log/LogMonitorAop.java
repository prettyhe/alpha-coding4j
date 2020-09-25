/**
 * Copyright
 */
package com.alpha.coding.common.log;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.alpha.coding.bo.base.MapThreadLocalAdaptor;
import com.alpha.coding.bo.trace.TimestampBase62UUIDTraceIdGenerator;
import com.alpha.coding.common.aop.assist.AopHelper;
import com.alpha.coding.common.utils.StringUtils;

import lombok.Data;

/**
 * LogMonitorAop
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Data
public class LogMonitorAop {

    private static final String TRACE_ID_KEY = "traceId";

    private static final String PROCEED_THROWABLE_MSG = "proceed error";

    private LogType logType;

    private Boolean logRequest;

    private Boolean logResponse;

    private Logor logor;

    private String customLogType;

    private boolean useItsLog = false;

    /**
     * 日志信息中需要排除的字段，半角逗号分隔
     */
    private String excludeInfoKeys = "";

    private ExtraMsgSupplier extraMsgSupplier;

    public LogMonitorAop() {
        this.logRequest = false;
        this.logResponse = false;
        this.logType = LogType.SERV_OUT;
    }

    public Object doMonitor(ProceedingJoinPoint joinPoint) throws Throwable {
        if (MDC.getMDCAdapter() == null) {
            return monitorHandler(joinPoint);
        }
        final String threadTraceId = MDC.get(TRACE_ID_KEY);
        if (threadTraceId == null) {
            MDC.put(TRACE_ID_KEY, genTraceId(joinPoint));
        }
        try {
            return monitorHandler(joinPoint);
        } finally {
            if (threadTraceId == null) {
                MDC.remove(TRACE_ID_KEY);
            }
        }
    }

    private Object monitorHandler(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        if (method.isAnnotationPresent(LogMonitorIgnore.class)) {
            return joinPoint.proceed();
        }
        // 进行log切面
        Class<?> targetClass = method.getDeclaringClass();
        try {
            final Object target = AopHelper.getTarget(joinPoint.getTarget());
            if (!Proxy.isProxyClass(target.getClass())) {
                targetClass = target.getClass();
            }
        } catch (Exception e) {
        }
        // 获取执行class的slf4j logger对象
        final Logger log = LoggerFactory.getLogger(targetClass);
        // 获取log条件信息，包括logType，request和response是否需要打印
        final LogCondition logCondition = getMethodLogMonitorAnnotation(method);
        if (logCondition.getExtraMsgSupplier() != null) {
            MapThreadLocalAdaptor.put("CURR_ExtraMsgSupplier", logCondition.getExtraMsgSupplier());
        }
        final String className = targetClass.getSimpleName();
        final String methodName = method.getName();
        final Class<?> returnType = method.getReturnType();
        final Object[] params = joinPoint.getArgs();
        final String[] paramNames = signature.getParameterNames();
        final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        final Long start = System.currentTimeMillis();
        long end = 0L;
        Object result = null;
        final String logId = LogIdThreadLocalHolder.getLogId();
        final String threadName = Thread.currentThread().getName();
        Class<? extends Throwable> exceptionClass = null;
        String exceptionMsg = null;
        try {
            if (logCondition.getLogType().equals(LogType.SERV_OUT)) {
                LogIdThreadLocalHolder.clearLogId(); // 先清空，后面再set
                LogIdThreadLocalHolder.setLogId(logId);
            }
            result = joinPoint.proceed(); // 需要注意方法内部是否有异步的方法
            end = System.currentTimeMillis();
            return result;
        } catch (Throwable t) {
            exceptionClass = t.getClass();
            exceptionMsg = t.getMessage();
            result = new ProceedThrowable(PROCEED_THROWABLE_MSG, t);
            throw t;
        } finally {
            if (end == 0) {
                end = System.currentTimeMillis();
            }
            Object finalObject = result;
            long finalEnd = end;
            try {
                LogContext context = new LogContext()
                        .setLog(logCondition.isUseItsLog() ? log : null)
                        .setThreadName(threadName)
                        .setLogId(logId)
                        .setParams(params)
                        .setParamNames(paramNames)
                        .setResponse(finalObject)
                        .setReturnType(returnType)
                        .setInterfaceName(className)
                        .setMethodName(methodName)
                        .setStartTime(start)
                        .setEndTime(finalEnd)
                        .setCondition(logCondition)
                        .setParameterAnnotations(parameterAnnotations)
                        .setExceptionClass(exceptionClass)
                        .setExceptionMsg(exceptionMsg);
                logor.doLog(context);
            } catch (Exception e) {
                log.error("log monitor error", e);
            }
            if (logCondition.getExtraMsgSupplier() != null) {
                MapThreadLocalAdaptor.remove("CURR_ExtraMsgSupplier");
            }
        }
    }

    /**
     * 根据方法取LogCondition
     * <p>
     * <li>优先取方法上声明的注解@LogMonitor中的LogCondition</li>
     * <li>其次取方法的类上声明的注解@LogMonitor中的LogCondition</li>
     * <li>再次取当前LogMonitorAop配置的LogCondition</li>
     * </p>
     */
    private LogCondition getMethodLogMonitorAnnotation(Method method) {
        Class<?> declaringClass = method.getDeclaringClass();
        if (method.isAnnotationPresent(LogMonitor.class)) {
            LogCondition conditionByMethod = new LogCondition(method.getAnnotation(LogMonitor.class));
            if (declaringClass.isAnnotationPresent(LogMonitor.class)) {
                if (StringUtils.isEmpty(conditionByMethod.getCustomLogType())) {
                    LogCondition conditionByClass = new LogCondition(declaringClass.getAnnotation(LogMonitor.class));
                    conditionByMethod.setCustomLogType(conditionByClass.getCustomLogType());
                }
            }
            return conditionByMethod;
        }
        if (declaringClass.isAnnotationPresent(LogMonitor.class)) {
            return new LogCondition(declaringClass.getAnnotation(LogMonitor.class));
        }
        LogCondition logCondition = new LogCondition();
        logCondition.setLogType(this.logType);
        logCondition.setLogRequest(this.logRequest != null && this.logRequest);
        logCondition.setLogResponse(this.logResponse != null && this.logResponse);
        logCondition.setCustomLogType(this.customLogType);
        logCondition.setUseItsLog(this.useItsLog);
        if (StringUtils.isNotBlank(this.excludeInfoKeys)) {
            Set<String> keys = new HashSet<>();
            for (String key : this.excludeInfoKeys.split(",")) {
                keys.add(key.trim());
            }
            logCondition.setExcludeInfoKeys(keys);
        }
        logCondition.setExtraMsgSupplier(this.extraMsgSupplier);
        return logCondition;
    }

    private String genTraceId(ProceedingJoinPoint joinPoint) {
        try {
            return TimestampBase62UUIDTraceIdGenerator.getInstance().traceId();
        } catch (Exception e) {
            return UUID.randomUUID().toString().replaceAll("-", "").toLowerCase();
        }
    }
}
