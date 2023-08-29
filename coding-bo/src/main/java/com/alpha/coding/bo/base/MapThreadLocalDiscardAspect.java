package com.alpha.coding.bo.base;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MapThreadLocalDiscardAspect {

    private static final String ASPECT_VERSION_KEY = "AspectVersion";
    private static final String KEYS_EXIST_MAP = "KEYS_EXIST_MAP_";

    private String[] discardKeys;

    public MapThreadLocalDiscardAspect() {
    }

    public MapThreadLocalDiscardAspect(String[] discardKeys) {
        this.discardKeys = discardKeys;
    }

    public static MapThreadLocalDiscardAspect of(String[] discardKeys) {
        return new MapThreadLocalDiscardAspect(discardKeys);
    }

    /**
     * 静态内部类工具
     */
    private static final class MapThreadLocal {

        private static final InheritableThreadLocal<Map<String, Object>> THREAD_LOCAL =
                new InheritableThreadLocal<Map<String, Object>>() {
                    @Override
                    protected Map<String, Object> childValue(Map<String, Object> parentValue) {
                        if (parentValue == null) {
                            return null;
                        }
                        return new LinkedHashMap<>(parentValue);
                    }
                };

        public static void put(String key, Object val) {
            if (key == null) {
                throw new IllegalArgumentException("key cannot be null");
            }
            Map<String, Object> map = THREAD_LOCAL.get();
            if (map == null) {
                synchronized(MapThreadLocal.class) {
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

    /**
     * 前置处理，将待丢弃的数据临时保存
     */
    public void doBefore() {
        if (discardKeys == null || discardKeys.length == 0) {
            return;
        }
        try {
            Integer version = (Integer) MapThreadLocal.get(ASPECT_VERSION_KEY);
            if (version == null) {
                version = 0;
            }
            if (version == 0) {
                MapThreadLocal.clear();
            }
            int thisVersion = version + 1; // 版本+1
            // 保存快照
            final Map<String, Object> existMap = new LinkedHashMap<>(discardKeys.length * 2);
            for (String key : discardKeys) {
                if (MapThreadLocalAdaptor.containsKey(key)) {
                    existMap.put(key, MapThreadLocalAdaptor.get(key));
                }
                MapThreadLocalAdaptor.remove(key); // 丢弃
            }
            MapThreadLocal.put(KEYS_EXIST_MAP + thisVersion, existMap);
            MapThreadLocal.put(ASPECT_VERSION_KEY, thisVersion);
        } catch (Exception e) {
            log.warn("doBefore error", e);
        }
    }

    /**
     * 后置处理，将丢弃的数据还原到MapThreadLocalAdaptor
     */
    @SuppressWarnings({"unchecked"})
    public void doAfter() {
        try {
            Integer version = (Integer) MapThreadLocal.get(ASPECT_VERSION_KEY);
            if (version == null) {
                version = 1;
            }
            final Map<String, Object> existMap = (Map<String, Object>) MapThreadLocal.get(KEYS_EXIST_MAP + version);
            if (existMap != null) {
                existMap.forEach(MapThreadLocalAdaptor::put);
            }
            MapThreadLocal.remove(KEYS_EXIST_MAP + version);
            version--;
            if (version <= 0) {
                MapThreadLocal.clear();
            }
        } catch (Exception e) {
            log.warn("doAfter error", e);
        }
    }

    /**
     * 增强处理，Runnable 结束时会恢复 MapThreadLocalAdaptor
     *
     * @param runnable    被增强函数
     * @param discardKeys 函数执行时丢弃的MapThreadLocalAdaptor的key
     */
    public static void enhance(Runnable runnable, String[] discardKeys) {
        final MapThreadLocalDiscardAspect aspect = MapThreadLocalDiscardAspect.of(discardKeys);
        try {
            aspect.doBefore();
            runnable.run();
        } finally {
            aspect.doAfter();
        }
    }

    /**
     * 增强处理，Consumer 结束时会恢复 MapThreadLocalAdaptor
     *
     * @param consumer    被增强函数
     * @param t           被增强函数输入值
     * @param discardKeys 函数执行时丢弃的MapThreadLocalAdaptor的key
     */
    public static <T> void enhance(Consumer<T> consumer, T t, String[] discardKeys) {
        final MapThreadLocalDiscardAspect aspect = MapThreadLocalDiscardAspect.of(discardKeys);
        try {
            aspect.doBefore();
            consumer.accept(t);
        } finally {
            aspect.doAfter();
        }
    }

    /**
     * 增强处理，Supplier 结束时会恢复 MapThreadLocalAdaptor
     *
     * @param supplier    被增强函数
     * @param discardKeys 函数执行时丢弃的MapThreadLocalAdaptor的key
     */
    public static <T> T enhance(Supplier<T> supplier, String[] discardKeys) {
        final MapThreadLocalDiscardAspect aspect = MapThreadLocalDiscardAspect.of(discardKeys);
        try {
            aspect.doBefore();
            return supplier.get();
        } finally {
            aspect.doAfter();
        }
    }

    /**
     * 增强处理，Function 结束时会恢复 MapThreadLocalAdaptor
     *
     * @param function    被增强函数
     * @param t           被增强函数输入值
     * @param discardKeys 函数执行时丢弃的MapThreadLocalAdaptor的key
     */
    public static <T, R> R enhance(Function<T, R> function, T t, String[] discardKeys) {
        final MapThreadLocalDiscardAspect aspect = MapThreadLocalDiscardAspect.of(discardKeys);
        try {
            aspect.doBefore();
            return function.apply(t);
        } finally {
            aspect.doAfter();
        }
    }

}
