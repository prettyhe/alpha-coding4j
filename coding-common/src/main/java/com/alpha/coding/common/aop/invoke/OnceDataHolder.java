package com.alpha.coding.common.aop.invoke;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.alpha.coding.bo.function.OneTimeSupplierHolder;
import com.alpha.coding.bo.function.SupplierHolder;

/**
 * OnceDataHolder
 *
 * @version 1.0
 * Date: 2021/9/22
 */
public class OnceDataHolder {

    private static final InheritableThreadLocal<Map<String, Object>> inheritableThreadLocal =
            new InheritableThreadLocal<Map<String, Object>>() {
                @Override
                protected Map<String, Object> childValue(Map<String, Object> parentValue) {
                    if (parentValue == null) {
                        return null;
                    }
                    return new LinkedHashMap<>(parentValue);
                }
            };

    public static void clear() {
        Map<String, Object> map = inheritableThreadLocal.get();
        if (map != null) {
            map.clear();
            inheritableThreadLocal.remove();
        }
    }

    public static void remove(String key) {
        Map<String, Object> map = inheritableThreadLocal.get();
        if (map != null) {
            map.remove(key);
        }
    }

    public static Set<String> getKeys() {
        Map<String, Object> map = inheritableThreadLocal.get();
        if (map != null) {
            return map.keySet();
        } else {
            return null;
        }
    }

    private static Map<String, Object> getMap() {
        Map<String, Object> map = inheritableThreadLocal.get();
        if (map == null) {
            synchronized(OnceDataHolder.class) {
                map = inheritableThreadLocal.get();
                if (map == null) {
                    map = new HashMap<>();
                    inheritableThreadLocal.set(map);
                }
            }
        }
        return map;
    }

    public static <T> void put(String key, Supplier<T> supplier) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        getMap().put(key, supplier);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getOnce(String key, Supplier<T> supplier, Boolean enableLog) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        if (supplier instanceof OneTimeSupplierHolder) {
            getMap().putIfAbsent(key, supplier);
            return supplier.get();
        }
        final OneTimeSupplierHolder<T> oneTimeSupplierHolder =
                (OneTimeSupplierHolder<T>) getMap().computeIfAbsent(key,
                        k -> enableLog != null ? new OneTimeSupplierHolder<>(k, supplier, enableLog)
                                : new OneTimeSupplierHolder<>(k, supplier));
        return oneTimeSupplierHolder.get();
    }

    public static <T> T getOnce(String key, Supplier<T> supplier) {
        return getOnce(key, supplier, null);
    }

    public static <T> T getOnceNonLog(String key, Supplier<T> supplier) {
        return getOnce(key, supplier, false);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getUntilNotNull(String key, Supplier<T> supplier) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        if (supplier instanceof SupplierHolder) {
            getMap().putIfAbsent(key, supplier);
            return supplier.get();
        }
        final SupplierHolder<T> supplierHolder =
                (SupplierHolder<T>) getMap().computeIfAbsent(key,
                        k -> new SupplierHolder<>(k, supplier));
        return supplierHolder.get();
    }

    @SuppressWarnings("unchecked")
    public static <T> Supplier<T> getSupplier(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        return (Supplier<T>) getMap().get(key);
    }

    @SuppressWarnings({"rawtypes"})
    public static Supplier rawSupplier(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        return (Supplier) getMap().get(key);
    }

    public static boolean hasSupplier(String key) {
        return getSupplier(key) != null;
    }

    public static boolean hasValue(String key) {
        final Supplier<Object> supplier = getSupplier(key);
        return supplier != null && supplier.get() != null;
    }

}
