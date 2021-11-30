package com.alpha.coding.common.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * ObjectUtils
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class ObjectUtils {

    /**
     * 调用目标类的"valueOf"方法进行转化
     */
    public static <T> T valueOf(Object val, Class<T> clz)
            throws IllegalAccessException, InvocationTargetException {
        if (val == null) {
            return null;
        }
        if (val.getClass().equals(clz)) {
            return (T) val;
        }
        for (Method method : clz.getDeclaredMethods()) {
            if (method.getName().equals("valueOf")) {
                final Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != 1 || !parameterTypes[0].equals(val.getClass())) {
                    continue;
                }
                return (T) method.invoke(null, val);
            }
        }
        throw new IllegalAccessException("NoSuchMethod");
    }
}
