package com.alpha.coding.common.mybatis.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * TableUpdateThreadLocal
 *
 * @version 1.0
 * Date: 2021/9/16
 */
public class TableUpdateThreadLocal {

    private static final ThreadLocal<Map<String, Object>> THREAD_LOCAL = ThreadLocal.withInitial(HashMap::new);
    private static final String UPDATE_TYPE = "update_type";
    private static final String UPDATE_REASON = "update_reason";

    public static void put(String key, Object val) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        Map<String, Object> map = THREAD_LOCAL.get();
        if (map == null) {
            synchronized(TableUpdateThreadLocal.class) {
                map = THREAD_LOCAL.get();
                if (map == null) {
                    map = new HashMap<>();
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
            return new HashMap<>(oldMap);
        } else {
            return null;
        }
    }

    public static void setUpdateType(String type) {
        put(UPDATE_TYPE, type);
    }

    public static Object getUpdateType() {
        return get(UPDATE_TYPE);
    }

    public static void setUpdateReason(String reason) {
        put(UPDATE_REASON, reason);
    }

    public static Object getUpdateReason() {
        return get(UPDATE_REASON);
    }

    public static void removeUpdateType() {
        remove(UPDATE_TYPE);
    }

    public static void removeUpdateReason() {
        remove(UPDATE_REASON);
    }

}
