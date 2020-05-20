package com.alpha.coding.common.log.dubbo;

import java.lang.reflect.Method;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.fastjson.JSON;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * LogMonitorFilter
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
@Activate(group = {Constants.CONSUMER, Constants.PROVIDER}, order = -100)
public class LogMonitorFilter implements Filter {

    private static final String OK = "200";
    private static final String FAIL = "600";
    private static final String TIMEOUT = "601";

    private static final String BLANK_STR = " ";
    private static final String EQUALS_SIGN = "=";

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        final LogItem item = new LogItem().setSt(System.currentTimeMillis());
        Result result = null;
        try {
            result = invoker.invoke(invocation);
            return result;
        } catch (Throwable t) {
            item.setExpType(t.getClass().getName()).setExpMsg(t.getMessage());
            throw t;
        } finally {
            try {
                doLog(item, invoker, invocation, result);
            } catch (Exception e) {
                if (log.isTraceEnabled()) {
                    log.warn("log LogItem error");
                }
            }
        }
    }

    private void doLog(LogItem item, Invoker<?> invoker, Invocation invocation, Result result) {
        if (invoker.getInterface().getClass().getName().equals("com.alibaba.dubbo.monitor.MonitorService")
                || invoker.getInterface().getClass().getName().equals("org.apache.dubbo.monitor.MonitorService")) {
            return;
        }
        if (log.isTraceEnabled()) {
            item.setEt(System.currentTimeMillis());
            if (invoker.getUrl() != null) {
                item.setSide(invoker.getUrl().getParameter(Constants.SIDE_KEY));
            }
            item.setInterfaceName(invoker.getInterface().getSimpleName());
            item.setMethodName(invocation.getMethodName());
            item.setArguments(invocation.getArguments());
            item.setCost(item.getEt() - item.getSt());
            if (item.getExpType() != null) {
                item.setRetCode(FAIL);
            } else if (invoker.getUrl() != null
                    && item.getCost() > invoker.getUrl().getMethodParameter(invocation.getMethodName(),
                    Constants.TIMEOUT_KEY, Integer.MAX_VALUE)) {
                item.setRetCode(TIMEOUT);
            } else {
                item.setRetCode(OK);
            }
            try {
                final Method method =
                        invoker.getInterface()
                                .getMethod(invocation.getMethodName(), invocation.getParameterTypes());
                final Class<?> returnType = method.getReturnType();
                item.setValue(buildString(result == null ? null : result.getValue(), returnType));
            } catch (NoSuchMethodException e) {
            }
            StringBuilder sb = new StringBuilder();
            sb.append("side").append(EQUALS_SIGN).append(item.getSide()).append(BLANK_STR);
            sb.append("[");
            sb.append("interface").append(EQUALS_SIGN).append(item.getInterfaceName()).append(BLANK_STR);
            sb.append("method").append(EQUALS_SIGN).append(item.getMethodName()).append(BLANK_STR);
            sb.append("costTime").append(EQUALS_SIGN).append(item.getCost()).append(BLANK_STR);
            sb.append("retCode").append(EQUALS_SIGN).append(item.getRetCode()).append(BLANK_STR);
            sb.append("st").append(EQUALS_SIGN).append(getTimeStr(item.getSt())).append(BLANK_STR);
            sb.append("et").append(EQUALS_SIGN).append(getTimeStr(item.getEt())).append(BLANK_STR);
            final String req = buildString(item.getArguments());
            if (req != null) {
                sb.append("req").append(EQUALS_SIGN).append(req).append(BLANK_STR);
            }
            if (item.getValue() != null) {
                sb.append("res").append(EQUALS_SIGN).append(item.getValue()).append(BLANK_STR);
            }
            if (item.getExpType() != null) {
                sb.append("expType").append(EQUALS_SIGN).append(item.getExpType()).append(BLANK_STR);
            }
            if (item.getExpMsg() != null) {
                sb.append("expMsg").append(EQUALS_SIGN).append(item.getExpMsg()).append(BLANK_STR);
            }
            sb.deleteCharAt(sb.lastIndexOf(BLANK_STR));
            sb.append("]");
            log.info(sb.toString());
        }
    }

    @Data
    @Accessors(chain = true)
    private static class LogItem {
        private String side;
        private String interfaceName;
        private String methodName;
        private String retCode;
        private long st;
        private long et;
        private long cost;
        private Object[] arguments;
        private String value;
        private String expType;
        private String expMsg;
    }

    private static String getTimeStr(long logTime) {
        String milliSecond = String.valueOf(logTime);
        return milliSecond.substring(0, milliSecond.length() - 3) + "."
                + milliSecond.substring(milliSecond.length() - 3);
    }

    private String buildString(Object[] arguments) {
        if (arguments == null || arguments.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        try {
            for (Object argument : arguments) {
                if (argument == null) {
                    sb.append("null").append("|");
                } else if (argument.getClass().isPrimitive()) {
                    sb.append(arguments.toString()).append("|");
                } else {
                    sb.append(JSON.toJSONString(argument)).append("|");
                }
            }
            if (sb.lastIndexOf("|") != -1) {
                sb.deleteCharAt(sb.lastIndexOf("|"));
            }
        } catch (Exception e) {
            if (log.isTraceEnabled()) {
                log.warn("build req to String fail");
            }
        }
        return sb.toString();
    }

    private String buildString(Object value, Class<?> type) {
        if (type.equals(Void.TYPE)) {
            return null;
        }
        if (type.isPrimitive()) {
            return String.valueOf(value);
        }
        try {
            return JSON.toJSONString(value);
        } catch (Exception e) {
            if (log.isTraceEnabled()) {
                log.warn("build res to String fail");
            }
            return String.valueOf(value);
        }
    }

}
