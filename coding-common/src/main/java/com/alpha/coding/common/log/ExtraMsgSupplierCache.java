package com.alpha.coding.common.log;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * ExtraMsgSupplierCache
 *
 * @version 1.0
 * Date: 2020/5/14
 */
public class ExtraMsgSupplierCache {

    private static ConcurrentMap<Class<? extends ExtraMsgSupplier>, ExtraMsgSupplier> CACHE = new ConcurrentHashMap<>();

    public static ExtraMsgSupplier getDefault(Class<? extends ExtraMsgSupplier> cls) {
        return CACHE.computeIfAbsent(cls, k -> {
            try {
                return cls.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
