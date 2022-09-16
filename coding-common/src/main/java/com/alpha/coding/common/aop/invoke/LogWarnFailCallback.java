package com.alpha.coding.common.aop.invoke;

import java.lang.reflect.Method;
import java.util.Arrays;

import lombok.extern.slf4j.Slf4j;

/**
 * LogWarnFailCallback
 *
 * @version 1.0
 * Date: 2022/6/17
 */
@Slf4j
public class LogWarnFailCallback implements FailCallback {

    @Override
    public Object callback(Method method, Object[] args, String failText) {
        log.warn("同步请求竞争失败:{},method={},args={}", failText, method.getName(), Arrays.toString(args));
        return null;
    }

}
