package com.alpha.coding.common.log;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.slf4j.Logger;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * LogContext
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Data
@Accessors(chain = true)
public class LogContext {

    private Logger log;
    private String threadName;
    private String logId;
    private Object[] params;
    private String[] paramNames;
    private Object response;
    private Class<?> targetClass;
    private Method targetMethod;
    private Class<?> returnType;
    private String interfaceName;
    private String methodName;
    private long startTime;
    private long endTime;
    private LogCondition condition;
    private Annotation[][] parameterAnnotations;
    private Class<? extends Throwable> exceptionClass;
    private String exceptionMsg;
    private String formattedRequest;

}
