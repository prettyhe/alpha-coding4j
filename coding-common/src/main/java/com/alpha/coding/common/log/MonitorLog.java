/**
 * Copyright
 */
package com.alpha.coding.common.log;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import com.alpha.coding.common.utils.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * MonitorLog
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class MonitorLog {

    private static final String DELIMITER_SPACE = " ";
    private static final String EQUAL_STR = "=";

    public static void logService(Logger logger, String threadName, String logId, String logType,
                                  long st, long et, String interfaceName, String method,
                                  long costTime, String resultCode, Map<String, String> extraData,
                                  LogCondition condition) {
        try {
            StringBuilder sb = new StringBuilder();
            final Set<String> excludeKeys = condition.getExcludeInfoKeys();
            append(sb, "threadName", threadName, excludeKeys);
            append(sb, "logId", StringUtils.isBlank(logId) ? null : logId, excludeKeys);
            append(sb, "logType", logType, excludeKeys);
            sb.append("[");
            append(sb, "interface", interfaceName, excludeKeys);
            append(sb, "method", method, excludeKeys);
            append(sb, "costTime", String.valueOf(costTime), excludeKeys);
            append(sb, "retCode", resultCode, excludeKeys);
            append(sb, "st", getTimeStr(st), excludeKeys);
            append(sb, "et", getTimeStr(et), excludeKeys);
            if (extraData != null && !extraData.isEmpty()) {
                for (Map.Entry<String, String> entry : extraData.entrySet()) {
                    append(sb, entry.getKey(), entry.getValue(), excludeKeys);
                }
            }
            if (condition.getExtraMsgSupplier() != null) {
                final String extraMsg = condition.getExtraMsgSupplier().supplier().get();
                if (extraMsg != null) {
                    sb.append(extraMsg).append(DELIMITER_SPACE);
                }
            }
            final int index = sb.lastIndexOf(DELIMITER_SPACE);
            if (index >= 0) {
                sb.deleteCharAt(index);
            }
            sb.append("]");
            (logger == null ? log : logger).info(sb.toString());
        } catch (Exception e) {
            log.error("logService error", e);
        }
    }

    private static void append(StringBuilder sb, String key, String val, Set<String> excludeKeys) {
        if (key == null || val == null || (excludeKeys != null && excludeKeys.contains(key))) {
            return;
        }
        sb.append(key).append(EQUAL_STR).append(val).append(DELIMITER_SPACE);
    }

    private static String getTimeStr(long logTime) {
        String milliSecond = String.valueOf(logTime);
        return milliSecond.substring(0, milliSecond.length() - 3) + "."
                + milliSecond.substring(milliSecond.length() - 3);
    }
}
