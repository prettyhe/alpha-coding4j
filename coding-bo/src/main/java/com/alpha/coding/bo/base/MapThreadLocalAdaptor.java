package com.alpha.coding.bo.base;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * MapThreadLocalAdaptor
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class MapThreadLocalAdaptor {

    private static InheritableThreadLocal<Map<String, Object>> inheritableThreadLocal =
            new InheritableThreadLocal<Map<String, Object>>() {
                @Override
                protected Map<String, Object> childValue(Map<String, Object> parentValue) {
                    if (parentValue == null) {
                        return null;
                    }
                    return new HashMap<String, Object>(parentValue);
                }
            };

    public static void put(String key, Object val) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        Map<String, Object> map = inheritableThreadLocal.get();
        if (map == null) {
            synchronized(MapThreadLocalAdaptor.class) {
                map = inheritableThreadLocal.get();
                if (map == null) {
                    map = new HashMap<String, Object>();
                    inheritableThreadLocal.set(map);
                }
            }
        }
        map.put(key, val);
    }

    public static Object get(String key) {
        Map<String, Object> map = inheritableThreadLocal.get();
        if ((map != null) && (key != null)) {
            return map.get(key);
        } else {
            return null;
        }
    }

    public static void remove(String key) {
        Map<String, Object> map = inheritableThreadLocal.get();
        if (map != null) {
            map.remove(key);
        }
    }

    public static void clear() {
        Map<String, Object> map = inheritableThreadLocal.get();
        if (map != null) {
            map.clear();
            inheritableThreadLocal.remove();
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

    public static Map<String, Object> getCopyOfContextMap() {
        Map<String, Object> oldMap = inheritableThreadLocal.get();
        if (oldMap != null) {
            return new HashMap<String, Object>(oldMap);
        } else {
            return null;
        }
    }

}
