package com.alpha.coding.common.log;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alpha.coding.common.utils.ClassUtils;

/**
 * ExtraMsgSupplierCache
 *
 * @version 1.0
 * Date: 2020/5/14
 */
public class ExtraMsgSupplierCache {

    private static final Map<Class<? extends ExtraMsgSupplier>, ExtraMsgSupplier> CACHE = new ConcurrentHashMap<>();

    public static ExtraMsgSupplier getDefault(Class<? extends ExtraMsgSupplier> cls) {
        return CACHE.computeIfAbsent(cls, k -> {
            try {
                return ClassUtils.newInstance(k);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                     InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
