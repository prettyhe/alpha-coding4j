package com.alpha.coding.common.log;

import java.lang.annotation.Annotation;

/**
 * Logor
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public interface Logor {

    /**
     * 记录日志
     *
     * @param context 日志上下文
     */
    void doLog(LogContext context);

    /**
     * 格式化请求参数
     *
     * @param paramNames       参数名数组
     * @param params           参数值数组
     * @param paramAnnotations 参数注解数组
     * @param condition        日志条件
     */
    String formatRequest(String[] paramNames, Object[] params, Annotation[][] paramAnnotations,
                         LogCondition condition);

}
