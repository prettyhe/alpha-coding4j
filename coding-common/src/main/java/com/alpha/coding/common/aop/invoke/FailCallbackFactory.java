package com.alpha.coding.common.aop.invoke;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alpha.coding.common.utils.ClassUtils;

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
                return ClassUtils.newInstance(k);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                    InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
