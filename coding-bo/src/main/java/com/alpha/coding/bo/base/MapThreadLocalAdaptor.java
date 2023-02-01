package com.alpha.coding.bo.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * MapThreadLocalAdaptor
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class MapThreadLocalAdaptor {

    private static final InheritableThreadLocal<Map<String, Object>> INHERITABLE_THREAD_LOCAL =
            new InheritableThreadLocal<Map<String, Object>>() {
                @Override
                protected Map<String, Object> childValue(Map<String, Object> parentValue) {
                    if (parentValue == null) {
                        return null;
                    }
                    return new HashMap<>(parentValue);
                }
            };

    public static boolean containsKey(String key) {
        Map<String, Object> map = INHERITABLE_THREAD_LOCAL.get();
        return map != null && map.containsKey(key);
    }

    public static void put(String key, Object val) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        Map<String, Object> map = INHERITABLE_THREAD_LOCAL.get();
        if (map == null) {
            synchronized(MapThreadLocalAdaptor.class) {
                map = INHERITABLE_THREAD_LOCAL.get();
                if (map == null) {
                    map = new HashMap<>();
                    INHERITABLE_THREAD_LOCAL.set(map);
                }
            }
        }
        map.put(key, val);
    }

    public static Object get(String key) {
        Map<String, Object> map = INHERITABLE_THREAD_LOCAL.get();
        if ((map != null) && (key != null)) {
            return map.get(key);
        } else {
            return null;
        }
    }

    @SuppressWarnings({"unchecked"})
    public static <T> T getAs(String key, Class<? extends T> type) {
        final Object val = get(key);
        if (val == null) {
            return null;
        }
        if (type.isAssignableFrom(val.getClass())) {
            return (T) val;
        }
        throw new ClassCastException(val.getClass().getName() + " can not cast to " + type.getName());
    }

    public static <T> T getAndConvert(String key, Function<Object, T> converter) {
        return converter.apply(get(key));
    }

    public static String getAsString(String key) {
        final Object val = get(key);
        return val == null ? null : String.valueOf(val);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Object getFirst(List<String> keys, Predicate predicate) {
        for (String key : keys) {
            final Object val = get(key);
            if (predicate == null || predicate.test(val)) {
                return val;
            }
        }
        return null;
    }

    public static void remove(String key) {
        Map<String, Object> map = INHERITABLE_THREAD_LOCAL.get();
        if (map != null) {
            map.remove(key);
        }
    }

    public static void clear() {
        Map<String, Object> map = INHERITABLE_THREAD_LOCAL.get();
        if (map != null) {
            map.clear();
            INHERITABLE_THREAD_LOCAL.remove();
        }
    }

    public static Set<String> getKeys() {
        Map<String, Object> map = INHERITABLE_THREAD_LOCAL.get();
        if (map != null) {
            return map.keySet();
        } else {
            return null;
        }
    }

    public static Map<String, Object> getCopyOfContextMap() {
        Map<String, Object> oldMap = INHERITABLE_THREAD_LOCAL.get();
        if (oldMap != null) {
            return new HashMap<>(oldMap);
        } else {
            return null;
        }
    }

    @SuppressWarnings({"unchecked"})
    public static <V> V computeIfAbsent(String key, Function<String, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        V v;
        if ((v = (V) get(key)) == null) {
            V newValue;
            if ((newValue = mappingFunction.apply(key)) != null) {
                put(key, newValue);
                return newValue;
            }
        }
        return v;
    }

    public static <V> V computeIfPresent(String key, BiFunction<String, Object, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        Object oldValue;
        if ((oldValue = get(key)) != null) {
            V newValue = remappingFunction.apply(key, oldValue);
            if (newValue != null) {
                put(key, newValue);
                return newValue;
            } else {
                remove(key);
                return null;
            }
        } else {
            return null;
        }
    }

}
