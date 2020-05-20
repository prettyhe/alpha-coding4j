package com.alpha.coding.common.log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.MDC;
import org.slf4j.spi.MDCAdapter;

import com.alpha.coding.bo.constant.Keys;
import com.alpha.coding.bo.trace.TraceIdGenerator;
import com.alpha.coding.bo.trace.UUIDTraceIdGenerator;
import com.google.common.collect.Maps;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * LogEnvAspect
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Data
@Accessors(chain = true)
public class LogEnvAspect {

    private static final String KEYS_EXIST_MAP_KEY = "keysExistMap";
    private List<String> keys = Arrays.asList("system", "appName", "host", "port", "module", "pid");
    private TraceIdGenerator traceIdGenerator = UUIDTraceIdGenerator.getInstance();

    /**
     * 执行切面逻辑
     *
     * @param joinPoint 切面
     */
    public Object doAspect(ProceedingJoinPoint joinPoint) throws Throwable {
        final AspectContext context = new AspectContext();
        try {
            doBefore(joinPoint, context);
            return joinPoint.proceed();
        } finally {
            doAfter(joinPoint, context);
        }
    }

    /**
     * 执行前钩子方法
     *
     * @param joinPoint 切面
     * @param context   处理上下文
     */
    protected void doBefore(ProceedingJoinPoint joinPoint, AspectContext context) {
        final MDCAdapter mdcAdapter = MDC.getMDCAdapter();
        if (mdcAdapter == null) {
            return;
        }
        final String traceId = mdcAdapter.get(Keys.TRACE_ID);
        context.fluentPut(Keys.TRACE_ID, traceId);
        if (traceId == null && traceIdGenerator != null) {
            mdcAdapter.put(Keys.TRACE_ID, traceIdGenerator.traceId());
        }
        final Map<String, Boolean> keysExistMap = Maps.newHashMap();
        context.fluentPut(KEYS_EXIST_MAP_KEY, keysExistMap);
        final List<String> keys = getKeys();
        if (keys == null || keys.size() == 0) {
            return;
        }
        for (String key : keys) {
            boolean exist = mdcAdapter.get(key) != null;
            keysExistMap.put(key, exist);
            if (!exist) {
                mdcAdapter.put(key, System.getProperty(key, ""));
            }
        }
    }

    /**
     * 执行后钩子方法
     *
     * @param joinPoint 切面
     * @param context   处理上下文
     */
    protected void doAfter(ProceedingJoinPoint joinPoint, AspectContext context) {
        final MDCAdapter mdcAdapter = MDC.getMDCAdapter();
        if (mdcAdapter == null) {
            return;
        }
        if (context.get(Keys.TRACE_ID) == null && getTraceIdGenerator() != null) {
            mdcAdapter.remove(Keys.TRACE_ID);
        }
        final List<String> keys = getKeys();
        final Map<String, Boolean> keysExistMap = (Map<String, Boolean>) context.get(KEYS_EXIST_MAP_KEY);
        if (keys != null) {
            for (String k : keys) {
                if (!keysExistMap.get(k)) {
                    mdcAdapter.remove(k);
                }
            }
        }
    }

    @Data
    protected static class AspectContext extends HashMap {

        public AspectContext fluentPut(Object key, Object value) {
            this.put(key, value);
            return this;
        }

    }

}
