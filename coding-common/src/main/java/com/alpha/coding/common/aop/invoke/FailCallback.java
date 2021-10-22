package com.alpha.coding.common.aop.invoke;

import java.lang.reflect.Method;

/**
 * FailCallback
 *
 * @version 1.0
 * Date: 2021/9/6
 */
public interface FailCallback {

    /**
     * 请求竞争失败回调
     *
     * @param method   当前调用的方法
     * @param args     当前调用的参数
     * @param failText 当前失败文案
     * @return 回调结果
     */
    Object callback(Method method, Object[] args, String failText);

}
