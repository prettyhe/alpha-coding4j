/**
 * Copyright
 */
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

    private boolean isRequestLog = false;

    private boolean isResponseLog = false;

    private boolean useItsLog = false;

    private Set<String> excludeInfoKeys;

    private ExtraMsgSupplier extraMsgSupplier;

    public LogCondition() {
    }

    public LogCondition(LogMonitor logMonitor) {
        this.logType = logMonitor.logType();
        this.isRequestLog = logMonitor.isRequestLog();
        this.isResponseLog = logMonitor.isResponseLog();
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
    }
}
