package com.alpha.coding.common.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.function.Supplier;

import javax.validation.constraints.NotNull;

import com.google.common.collect.Maps;

/**
 * MapUtils
 *
 * @version 1.0
 * Date: 2016-01-21
 */
public class MapUtils {

    public static <K, CK> void putToChildMap(Map<K, Object> parentMap, K key, CK childKey, Object childValue,
                                             final boolean linked) {
        putToChildMap(parentMap, key, childKey, childValue,
                () -> linked ? Maps.newLinkedHashMap() : Maps.newConcurrentMap());
    }

    public static <K, CK> void putToChildMap(Map<K, Object> parentMap, K key, CK childKey, Object childValue,
                                             Supplier<Map<CK, Object>> supplier) {
        if (parentMap.get(key) == null) {
            synchronized(parentMap) {
                if (parentMap.get(key) == null) {
                    parentMap.put(key, supplier.get());
                }
            }
        }
        Map<CK, Object> childMap = (Map<CK, Object>) parentMap.get(key);
        if (childValue != null && childValue instanceof Map) {
            if (childMap.get(childKey) == null) {
                childMap.put(childKey, childValue);
            } else {
                if (childMap.get(childKey) instanceof Map) {
                    ((Map) childMap.get(childKey)).putAll((Map) childValue);
                }
            }
        }
        childMap.put(childKey, childValue);
    }

    public static <K, V> Map<String, V> toStringKeyMap(Map<K, V> map) {
        if (map == null) {
            return null;
        }
        Map<String, V> ret = Maps.newHashMap();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            ret.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return ret;
    }

    public static <K, V> V createAndGet(Map<K, V> map, K key, Class<V> clz)
            throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        V v = map.get(key);
        if (v == null) {
            synchronized(map) {
                v = map.get(key);
                if (v == null) {
                    v = ClassUtils.newInstance(clz);
                    map.put(key, v);
                }
            }
        }
        return v;
    }

    public static <K, V> V getWithDefault(Map<K, V> map, K key, @NotNull V v) {
        V val = map.get(key);
        if (val == null) {
            synchronized(map) {
                val = map.get(key);
                if (val == null) {
                    val = v;
                    map.put(key, val);
                }
            }
        }
        return val;
    }

}
