package com.alpha.coding.common.log;

import java.util.HashSet;
import java.util.Set;

import lombok.Data;

/**
 * LogCondition
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Data
public class LogCondition {

    /**
     * log类型，默认是{@link LogType}下的对外提供服务
     */
    private LogType logType = LogType.SERV_OUT;

    private String customLogType;

    private boolean logRequest = false;

    private boolean logResponse = false;

    private boolean lazyFormatRequest = false;

    private boolean useItsLog = false;

    private Set<String> excludeInfoKeys;

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

    public LogCondition() {
    }

    public LogCondition(LogMonitor logMonitor) {
        this.logType = logMonitor.logType();
        this.logRequest = logMonitor.logRequest();
        this.logResponse = logMonitor.logResponse();
        this.lazyFormatRequest = logMonitor.lazyFormatRequest();
        this.customLogType = logMonitor.customLogType();
        this.useItsLog = logMonitor.useItsLog();
        if (logMonitor.excludeInfoKeys().length > 0) {
            if (this.excludeInfoKeys == null) {
                this.excludeInfoKeys = new HashSet<>();
            }
            for (String key : logMonitor.excludeInfoKeys()) {
                this.excludeInfoKeys.add(key.trim());
            }
        }
        this.extraMsgSupplier = ExtraMsgSupplierCache.getDefault(logMonitor.extraMsgSupplier());
        final LogDataPath logDataPath = logMonitor.logDataPath();
        this.reqIgnoreFieldPath = logDataPath.reqIgnoreFieldPath();
        this.resIgnoreFieldPath = logDataPath.resIgnoreFieldPath();
        this.reqRetainFieldPath = logDataPath.reqRetainFieldPath();
        this.resRetainFieldPath = logDataPath.resRetainFieldPath();
    }
}
