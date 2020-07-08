/**
 * Copyright
 */
package com.alpha.coding.common.log;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.alibaba.fastjson.JSON;
import com.alpha.coding.common.utils.StringUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lombok.extern.slf4j.Slf4j;

/**
 * AbstractLogor
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public abstract class AbstractLogor implements Logor {

    protected static final String EMPTY = "";

    /**
     * 响应结果正常
     */
    protected static final String OK = "200";
    /**
     * 响应结果未知
     */
    protected static final String UNKNOWN = "10000";
    /**
     * 系统错误
     */
    protected static final String SYSTEM_ERROR = "10002";

    protected static final String REQUEST_KEY = "req";
    protected static final String RESPONSE_KEY = "res";
    protected static final String EXCEPTION_TYPE_KEY = "expType";
    protected static final String EXCEPTION_MSG_KEY = "expMsg";

    private static final ConcurrentMap<String, Boolean> CAN_NOT_JSON_MAP = new ConcurrentHashMap<>(64);

    static {
        CAN_NOT_JSON_MAP.put("javax.servlet.ServletRequest", true);
        CAN_NOT_JSON_MAP.put("javax.servlet.ServletResponse", true);
        CAN_NOT_JSON_MAP.put("javax.servlet.http.HttpSession", true);
    }

    /**
     * 钩子方法，子类需要实现
     */
    protected abstract String getResponseCode(Class<?> responseClazz, Object response) throws Exception;

    @Override
    public void doLog(LogContext context) {
        doApiLog(context);
    }

    /**
     * 对外提供接口的日志记录方法
     */
    private void doApiLog(LogContext context) {
        try {
            String logTypeStr;
            if (StringUtils.isNotBlank(context.getCondition().getCustomLogType())) {
                logTypeStr = context.getCondition().getCustomLogType();
            } else {
                LogType logType = context.getCondition().getLogType();
                logTypeStr = logType == null ? null : logType.getType();
            }
            /**
             * 处理抛出异常，视为系统错误
             */
            if (context.getResponse() instanceof ProceedThrowable) {
                Map<String, String> extraData = Maps.newLinkedHashMap();
                extraData.put(REQUEST_KEY, getFormatStr(context.getParamNames(), context.getParams(),
                        context.getParameterAnnotations()));
                extraData.put(RESPONSE_KEY, null);
                if (context.getExceptionClass() != null) {
                    extraData.put(EXCEPTION_TYPE_KEY, context.getExceptionClass().getName());
                }
                if (context.getExceptionMsg() != null) {
                    extraData.put(EXCEPTION_MSG_KEY, context.getExceptionMsg());
                }
                String resultCode = SYSTEM_ERROR;
                MonitorLog.logService(context.getLog(), context.getThreadName(),
                        context.getLogId(), logTypeStr,
                        context.getStartTime(), context.getEndTime(),
                        context.getInterfaceName(), context.getMethodName(),
                        (context.getEndTime() - context.getStartTime()),
                        resultCode, extraData, context.getCondition());
                return;
            }
            if (context.getResponse() == null) {
                Map<String, String> extraData = Maps.newLinkedHashMap();
                extraData.put(REQUEST_KEY, getFormatStr(context.getParamNames(), context.getParams(),
                        context.getParameterAnnotations()));
                extraData.put(RESPONSE_KEY, null);
                String resultCode = context.getReturnType().equals(void.class) ? OK : UNKNOWN;
                MonitorLog.logService(context.getLog(), context.getThreadName(),
                        context.getLogId(), logTypeStr,
                        context.getStartTime(), context.getEndTime(),
                        context.getInterfaceName(), context.getMethodName(),
                        (context.getEndTime() - context.getStartTime()),
                        resultCode, extraData, context.getCondition());
                return;
            }
            Class responseClazz = context.getReturnType().getClass();
            Map<String, String> extraData = Maps.newLinkedHashMap();
            if (context.getCondition().isRequestLog()) {
                extraData.put(REQUEST_KEY, getFormatStr(context.getParamNames(), context.getParams(),
                        context.getParameterAnnotations()));
            }
            if (context.getCondition().isResponseLog()) {
                String resultStr = getFormatStr(new String[] {"return"}, new Object[] {context.getResponse()}, null);
                extraData.put(RESPONSE_KEY, resultStr);
            }
            MonitorLog.logService(context.getLog(), context.getThreadName(),
                    context.getLogId(), logTypeStr,
                    context.getStartTime(), context.getEndTime(),
                    context.getInterfaceName(), context.getMethodName(),
                    (context.getEndTime() - context.getStartTime()),
                    String.valueOf(getResponseCode(responseClazz, context.getResponse())),
                    extraData, context.getCondition());
        } catch (Exception e) {
            (context.getLog() == null ? log : context.getLog()).error("doLog fail", e);
        }
    }

    protected String getFormatStr(String[] paramNames, Object[] params, Annotation[][] parameterAnnotations) {

        final StringBuffer paramStr = new StringBuffer();

        if (params == null || params.length == 0) {
            return EMPTY;
        }
        for (int i = 0; i < params.length; i++) {
            /**
             * 注意，json化的对象不能是未关闭的对象，比如流，此处做了少量枚举
             */
            if (canNotJson(params[i])) {
                continue;
            }
            if (parameterAnnotations != null && parameterAnnotations.length > i) {
                final Annotation[] ans = parameterAnnotations[i];
                if (ans != null) {
                    LogMonitorIgnore ignore = null;
                    for (Annotation an : ans) {
                        if (an instanceof LogMonitorIgnore) {
                            ignore = (LogMonitorIgnore) an;
                            break;
                        }
                    }
                    if (ignore != null) {
                        continue;
                    }
                }
            }
            try {
                paramStr.append(JSON.toJSONString(params[i])).append("|");
            } catch (Throwable throwable) {
                log.warn("format parameter name={}, class={} fail",
                        paramNames != null && paramNames.length > i ? paramNames[i] : null,
                        params[i].getClass().getName());
            }
        }

        return paramStr.length() > 0 ? paramStr.substring(0, paramStr.length() - 1) : paramStr.toString();
    }

    protected boolean canNotJson(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof InputStream || obj instanceof OutputStream) {
            return true;
        }
        final Class<?> clz = obj.getClass();
        if (CAN_NOT_JSON_MAP.containsKey(clz.getName())) {
            return true;
        }
        boolean add = false;
        for (String key : Lists.newArrayList(CAN_NOT_JSON_MAP.keySet())) {
            try {
                add = Class.forName(key).isAssignableFrom(clz);
                if (add) {
                    break;
                }
            } catch (ClassNotFoundException e) {
            }
        }
        if (add) {
            CAN_NOT_JSON_MAP.put(clz.getName(), true);
        }
        return add;
    }

}
