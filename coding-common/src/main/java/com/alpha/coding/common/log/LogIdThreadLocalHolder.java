package com.alpha.coding.common.log;

/**
 * LogIdThreadLocalHolder
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class LogIdThreadLocalHolder {

    /**
     * log id holder
     */
    private static final ThreadLocal<String> LOG_ID_HOLDER = new ThreadLocal<String>();

    /**
     * @param logId set log id
     */
    public static void setLogId(String logId) {
        if (logId != null) {
            LOG_ID_HOLDER.set(logId);
        }
    }

    /**
     * @return log id
     */
    public static String getLogId() {
        return LOG_ID_HOLDER.get();
    }

    /**
     * clear current log id under current thread scope
     */
    public static void clearLogId() {
        LOG_ID_HOLDER.remove();
    }

}
