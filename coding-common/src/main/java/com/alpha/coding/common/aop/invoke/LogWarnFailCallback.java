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
    public Object onLocalLockFail(Method method, Object[] args, Object currentData, String failText) {
        log.warn("同步请求本地加锁失败:{},method={},args={}", failText, method.getName(), Arrays.toString(args));
        return FailCallback.super.onLocalLockFail(method, args, currentData, failText);
    }

    @Override
    public Object onLocalWaitTimeout(Method method, Object[] args, Object currentData, String failText) {
        log.warn("同步请求本地等待超时:{},method={},args={}", failText, method.getName(), Arrays.toString(args));
        return FailCallback.super.onLocalWaitTimeout(method, args, currentData, failText);
    }

    @Override
    public Object onLocalWaitInterrupted(Method method, Object[] args, Object currentData, String failText) {
        log.warn("同步请求本地等待中断:{},method={},args={}", failText, method.getName(), Arrays.toString(args));
        return FailCallback.super.onLocalWaitInterrupted(method, args, currentData, failText);
    }

    @Override
    public Object callback(Method method, Object[] args, String failText) {
        log.warn("同步请求竞争失败:{},method={},args={}", failText, method.getName(), Arrays.toString(args));
        return null;
    }

}
