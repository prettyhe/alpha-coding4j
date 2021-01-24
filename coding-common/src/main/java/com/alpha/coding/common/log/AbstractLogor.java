/**
 * Copyright
 */
package com.alpha.coding.common.log;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import com.alibaba.fastjson.JSON;
import com.alpha.coding.common.utils.StringUtils;
import com.alpha.coding.common.utils.json.FastjsonJsonProvider;
import com.alpha.coding.common.utils.json.FastjsonMappingProvider;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

/**
 * AbstractLogor
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public abstract class AbstractLogor implements Logor {

    /**
     * 空字符串
     */
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
    /**
     * 通用参数
     */
    protected static final String REQUEST_KEY = "req";
    protected static final String RESPONSE_KEY = "res";
    protected static final String EXCEPTION_TYPE_KEY = "expType";
    protected static final String EXCEPTION_MSG_KEY = "expMsg";
    /**
     * 不能转json的类型缓存
     */
    private static final ConcurrentMap<String, Boolean> CAN_NOT_JSON_MAP = new ConcurrentHashMap<>(64);
    /**
     * JsonPath缓存
     */
    private static final ConcurrentMap<String, JsonPath> JSON_PATH_MAP = new ConcurrentHashMap<>(64);
    /**
     * JsonPath使用的配置
     *
     * @see com.jayway.jsonpath.Configuration
     * @see <a href="https://github.com/json-path/JsonPath">https://github.com/json-path/JsonPath</a>
     */
    private static final Configuration JSON_CONFIGURATION = new Configuration.ConfigurationBuilder()
            .jsonProvider(new FastjsonJsonProvider())
            .mappingProvider(new FastjsonMappingProvider())
            .options(Configuration.defaultConfiguration().getOptions())
            .evaluationListener(Configuration.defaultConfiguration().getEvaluationListeners())
            .build();
    /**
     * 请求参数的JsonPath格式
     */
    private static final Pattern REQ_PATH_PATTERN = Pattern.compile("^(\\*|\\d+)\\.\\$.*");

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
     *
     * @param context 上下文
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
            // 处理抛出异常，视为系统错误
            if (context.getResponse() instanceof ProceedThrowable) {
                final Map<String, String> extraData = Maps.newLinkedHashMap();
                handleLogReq(context, extraData);
                extraData.put(RESPONSE_KEY, null);
                if (context.getExceptionClass() != null) {
                    extraData.put(EXCEPTION_TYPE_KEY, context.getExceptionClass().getName());
                }
                if (context.getExceptionMsg() != null) {
                    extraData.put(EXCEPTION_MSG_KEY, context.getExceptionMsg());
                }
                MonitorLog.logService(context.getLog(), context.getThreadName(),
                        context.getLogId(), logTypeStr,
                        context.getStartTime(), context.getEndTime(),
                        context.getInterfaceName(), context.getMethodName(),
                        (context.getEndTime() - context.getStartTime()),
                        SYSTEM_ERROR, extraData, context.getCondition());
                return;
            }
            if (context.getResponse() == null) {
                final Map<String, String> extraData = Maps.newLinkedHashMap();
                handleLogReq(context, extraData);
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
            Class<?> responseClazz = context.getReturnType();
            final Map<String, String> extraData = Maps.newLinkedHashMap();
            handleLogReq(context, extraData);
            handleLogRes(context, extraData);
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

    /**
     * 解析请求参数的JsonPath Map
     */
    private Map<Integer, List<String>> parseReqPathMap(String[] paramNames, String[] fieldPaths) {
        final Map<Integer, List<String>> pathMap = new TreeMap<>();
        if (fieldPaths != null && fieldPaths.length > 0) {
            Arrays.stream(fieldPaths).filter(StringUtils::isNotBlank).forEach(p -> {
                if (REQ_PATH_PATTERN.matcher(p).matches()) {
                    try {
                        final String index = p.split("\\.")[0];
                        final String path = p.substring(index.length() + 1);
                        if ("*".equals(index)) {
                            if (paramNames != null && paramNames.length > 0) {
                                IntStream.range(0, paramNames.length)
                                        .forEach(i -> pathMap.computeIfAbsent(i,
                                                k -> new ArrayList<>()).add(path));
                            }
                        } else {
                            pathMap.computeIfAbsent(Integer.valueOf(index),
                                    k -> new ArrayList<>()).add(path);
                        }
                    } catch (Exception e) {
                        log.warn("parse path {} fail, {}", p, e.getMessage());
                    }
                }
            });
        }
        return pathMap;
    }

    /**
     * 解析响应参数的JsonPath Map
     */
    private Map<Integer, List<String>> parseResPathMap(String[] fieldPaths) {
        final Map<Integer, List<String>> pathMap = new TreeMap<>();
        if (fieldPaths != null && fieldPaths.length > 0) {
            Arrays.stream(fieldPaths).filter(StringUtils::isNotBlank)
                    .forEach(p -> pathMap.computeIfAbsent(0, k -> new ArrayList<>()).add(p));
        }
        return pathMap;
    }

    /**
     * 处理请求参数
     */
    private void handleLogReq(LogContext context, Map<String, String> extraData) {
        if (context.getCondition().isLogRequest()) {
            final String[] paramNames = context.getParamNames();
            extraData.put(REQUEST_KEY, formatParams(paramNames, context.getParams(),
                    context.getParameterAnnotations(),
                    parseReqPathMap(paramNames, context.getCondition().getReqIgnoreFieldPath()),
                    parseReqPathMap(paramNames, context.getCondition().getReqRetainFieldPath())));
        }
    }

    /**
     * 处理响应参数
     */
    private void handleLogRes(LogContext context, Map<String, String> extraData) {
        if (context.getCondition().isLogResponse()) {
            extraData.put(RESPONSE_KEY, formatParams(new String[] {"return"},
                    new Object[] {context.getResponse()},
                    new Annotation[][] {context.getTargetMethod().getDeclaredAnnotations()},
                    parseResPathMap(context.getCondition().getResIgnoreFieldPath()),
                    parseResPathMap(context.getCondition().getResRetainFieldPath())));
        }
    }

    /**
     * 格式化请求/响应参数
     */
    protected String formatParams(String[] paramNames, Object[] params, Annotation[][] parameterAnnotations,
                                  Map<Integer, List<String>> ignoreJsonPathMap,
                                  Map<Integer, List<String>> retainJsonPathMap) {

        final StringBuilder paramStr = new StringBuilder();

        if (params == null || params.length == 0) {
            return EMPTY;
        }
        for (int i = 0; i < params.length; i++) {
            final Object param = params[i];
            // 检查是否可以转json
            if (canNotJson(param)) {
                continue;
            }
            // 检查是否忽略该参数
            if (parameterAnnotations != null && parameterAnnotations.length > i) {
                final Annotation[] ans = parameterAnnotations[i];
                if (ans != null && ans.length > 0) {
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
            if (param == null) {
                paramStr.append(param).append("|");
                continue;
            }
            final String parameterName = paramNames != null && paramNames.length > i ? paramNames[i] : null;
            try {
                String json = JSON.toJSONString(param);
                DocumentContext context = null;
                // 配置中的删除path
                if (ignoreJsonPathMap != null) {
                    final List<String> list = ignoreJsonPathMap.get(i);
                    if (list != null && list.size() > 0) {
                        for (String path : list) {
                            try {
                                final JsonPath jsonPath = JSON_PATH_MAP.computeIfAbsent(path, JsonPath::compile);
                                if (context == null) {
                                    context = JsonPath.using(JSON_CONFIGURATION).parse(json);
                                }
                                context.delete(jsonPath);
                            } catch (Exception e) {
                                log.warn("delete {} from {} fail, {}", path, parameterName, e.getMessage());
                            }
                        }
                    }
                }
                // 配置中的保留path
                if (retainJsonPathMap != null) {
                    final List<String> list = retainJsonPathMap.get(i);
                    if (list != null && list.size() > 0) {
                        final StringBuilder retainJson = new StringBuilder();
                        for (String path : list) {
                            try {
                                final JsonPath jsonPath = JSON_PATH_MAP.computeIfAbsent(path, JsonPath::compile);
                                if (context == null) {
                                    context = JsonPath.using(JSON_CONFIGURATION).parse(json);
                                }
                                final Object read = context.read(jsonPath);
                                retainJson.append(read == null ? (String) null : read.toString()).append(";");
                            } catch (Exception e) {
                                log.warn("retain {} from {} fail, {}", path, parameterName, e.getMessage());
                            }
                        }
                        if (retainJson.length() > 0) {
                            retainJson.deleteCharAt(retainJson.lastIndexOf(";"));
                        }
                        json = retainJson.toString();
                        paramStr.append(json).append("|");
                        continue;
                    }
                }
                if (context != null) {
                    json = context.jsonString();
                }
                paramStr.append(json).append("|");
            } catch (Throwable throwable) {
                log.warn("format parameter fail, name={}, class={}, msg={}",
                        parameterName, param.getClass().getName(), throwable.getMessage());
            }
        }

        return paramStr.length() > 0 ? paramStr.substring(0, paramStr.length() - 1) : paramStr.toString();
    }

    /**
     * 检查是否不能转json
     * <p/>
     * json化的对象不能是未关闭的对象，比如流，此处做了少量枚举
     *
     * @param obj 对象
     * @return 检查结果
     */
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
                // nothing
            }
        }
        if (add) {
            CAN_NOT_JSON_MAP.put(clz.getName(), true);
        }
        return add;
    }

}
