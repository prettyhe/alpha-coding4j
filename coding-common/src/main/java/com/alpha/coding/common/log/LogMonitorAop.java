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

    /**
     * 请求参数中忽略打印的字段的JsonPath，
     * 以参数下标(从0开始，*表示所有参数)开始，如0.$['name']，即从第一个点号之后的为该参数的真实JsonPath
     *
     * @see com.jayway.jsonpath.JsonPath
     * @see <a href="https://github.com/json-path/JsonPath">https://github.com/json-path/JsonPath</a>
     */
    private String[] reqIgnoreFieldPath;

    /**
     * 响应参数中忽略打印的字段的JsonPath
     *
     * @see com.jayway.jsonpath.JsonPath
     * @see <a href="https://github.com/json-path/JsonPath">https://github.com/json-path/JsonPath</a>
     */
    private String[] resIgnoreFieldPath;
    /**
     * 请求参数中保留打印的字段的JsonPath，
     * 以参数下标(从0开始，*表示所有参数)开始，如0.$['name']，即从第一个点号之后的为该参数的真实JsonPath
     *
     * @see com.jayway.jsonpath.JsonPath
     * @see <a href="https://github.com/json-path/JsonPath">https://github.com/json-path/JsonPath</a>
     */
    private String[] reqRetainFieldPath;

    /**
     * 响应参数中保留打印的字段的JsonPath
     *
     * @see com.jayway.jsonpath.JsonPath
     * @see <a href="https://github.com/json-path/JsonPath">https://github.com/json-path/JsonPath</a>
     */
    private String[] resRetainFieldPath;

    public LogMonitorAop() {
        this.logRequest = false;
        this.logResponse = false;
        this.logType = LogType.SERV_OUT;
    }

    /**
     * 切面方法
     *
     * @param joinPoint 切入点
     * @return 方法执行返回结果
     */
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

    /**
     * 切面方法处理
     *
     * @param joinPoint 切入点
     * @return 方法执行返回结果
     */
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
            // nothing
        }
        // 获取执行class的slf4j logger对象
        final Logger log = LoggerFactory.getLogger(targetClass);
        // 获取log条件信息，包括logType，request和response是否需要打印
        final LogCondition logCondition = parseMethodLogCondition(method);
        if (logCondition.getExtraMsgSupplier() != null) {
            MapThreadLocalAdaptor.put("CURR_ExtraMsgSupplier", logCondition.getExtraMsgSupplier());
        }
        final String className = targetClass.getSimpleName();
        final String methodName = method.getName();
        final Class<?> returnType = method.getReturnType();
        final Object[] params = joinPoint.getArgs();
        final String[] paramNames = signature.getParameterNames();
        final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        final long start = System.currentTimeMillis();
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
            long finalEnd = end;
            try {
                LogContext context = new LogContext()
                        .setLog(logCondition.isUseItsLog() ? log : null)
                        .setThreadName(threadName)
                        .setLogId(logId)
                        .setParams(params)
                        .setParamNames(paramNames)
                        .setResponse(result)
                        .setTargetClass(targetClass)
                        .setTargetMethod(method)
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
     * <li>如果方法标注@LogDataPath，以标注的@LogDataPath覆盖默认的LogCondition中的LogDataPath配置</li>
     * </p>
     */
    private LogCondition parseMethodLogCondition(Method method) {
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
        final LogCondition logCondition = new LogCondition();
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
        logCondition.setReqIgnoreFieldPath(this.reqIgnoreFieldPath);
        logCondition.setResIgnoreFieldPath(this.resIgnoreFieldPath);
        logCondition.setReqRetainFieldPath(this.reqRetainFieldPath);
        logCondition.setResRetainFieldPath(this.resRetainFieldPath);
        // 如果方法标注@LogDataPath，则取标注配置
        if (method.isAnnotationPresent(LogDataPath.class)) {
            final LogDataPath logDataPath = method.getDeclaredAnnotation(LogDataPath.class);
            logCondition.setReqIgnoreFieldPath(logDataPath.reqIgnoreFieldPath());
            logCondition.setResIgnoreFieldPath(logDataPath.resIgnoreFieldPath());
            logCondition.setReqRetainFieldPath(logDataPath.reqRetainFieldPath());
            logCondition.setResRetainFieldPath(logDataPath.resRetainFieldPath());
        }
        return logCondition;
    }

    /**
     * 生成traceId
     *
     * @param joinPoint 切入点
     * @return traceId
     */
    private String genTraceId(ProceedingJoinPoint joinPoint) {
        try {
            return TimestampBase62UUIDTraceIdGenerator.getInstance().traceId();
        } catch (Exception e) {
            return UUID.randomUUID().toString().replaceAll("-", "").toLowerCase();
        }
    }

}
