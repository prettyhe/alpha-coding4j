package com.alpha.coding.common.aop.invoke;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FailCallbackFactory
 *
 * @version 1.0
 * Date: 2021/9/6
 */
public class FailCallbackFactory {

    private static final Map<Class<? extends FailCallback>, FailCallback> MAP = new ConcurrentHashMap<>(16);

    public static FailCallback instance(Class<? extends FailCallback> clz) {
        return MAP.computeIfAbsent(clz, k -> {
            try {
                return k.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
