package com.alpha.coding.common.mybatis.common;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * TableUpdateBeforeControl
 *
 * @version 1.0
 * Date: 2021/9/16
 */
public class TableUpdateBeforeControl {

    private static final ThreadLocal<Map<String, Object>> THREAD_LOCAL = ThreadLocal.withInitial(LinkedHashMap::new);

    public static void put(String key, Object val) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        Map<String, Object> map = THREAD_LOCAL.get();
        if (map == null) {
            synchronized(TableUpdateBeforeControl.class) {
                map = THREAD_LOCAL.get();
                if (map == null) {
                    map = new LinkedHashMap<>();
                    THREAD_LOCAL.set(map);
                }
            }
        }
        map.put(key, val);
    }

    public static Object get(String key) {
        Map<String, Object> map = THREAD_LOCAL.get();
        if ((map != null) && (key != null)) {
            return map.get(key);
        } else {
            return null;
        }
    }

    public static void remove(String key) {
        Map<String, Object> map = THREAD_LOCAL.get();
        if (map != null) {
            map.remove(key);
        }
    }

    public static void clear() {
        Map<String, Object> map = THREAD_LOCAL.get();
        if (map != null) {
            map.clear();
            THREAD_LOCAL.remove();
        }
    }

    public static Set<String> getKeys() {
        Map<String, Object> map = THREAD_LOCAL.get();
        if (map != null) {
            return map.keySet();
        } else {
            return null;
        }
    }

    public static Map<String, Object> getCopyOfContextMap() {
        Map<String, Object> oldMap = THREAD_LOCAL.get();
        if (oldMap != null) {
            return new LinkedHashMap<>(oldMap);
        } else {
            return null;
        }
    }

}
