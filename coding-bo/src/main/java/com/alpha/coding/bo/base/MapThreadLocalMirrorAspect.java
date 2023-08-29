package com.alpha.coding.bo.base;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MapThreadLocalMirrorAspect {

    private static final String ASPECT_VERSION_KEY = "AspectVersion";
    private static final String MIRROR_KEY_PREFIX = "Mirror_";

    private static final MapThreadLocalMirrorAspect INSTANCE = new MapThreadLocalMirrorAspect();

    public static MapThreadLocalMirrorAspect getDefault() {
        return INSTANCE;
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
     * 前置处理，拷贝MapThreadLocalAdaptor镜像
     */
    public void doBefore() {
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
            MapThreadLocal.put(MIRROR_KEY_PREFIX + thisVersion, MapThreadLocalAdaptor.getCopyOfContextMap());
            MapThreadLocal.put(ASPECT_VERSION_KEY, thisVersion);
        } catch (Exception e) {
            log.warn("doBefore error", e);
        }
    }

    /**
     * 后置处理，清理并还原MapThreadLocalAdaptor镜像
     */
    @SuppressWarnings({"unchecked"})
    public void doAfter() {
        try {
            Integer version = (Integer) MapThreadLocal.get(ASPECT_VERSION_KEY);
            if (version == null) {
                version = 1;
            }
            final Map<String, Object> map = (Map<String, Object>) MapThreadLocal.get(MIRROR_KEY_PREFIX + version);
            MapThreadLocalAdaptor.clear();
            // 还原快照
            if (map != null) {
                map.forEach(MapThreadLocalAdaptor::put);
            }
            MapThreadLocal.remove(MIRROR_KEY_PREFIX + version);
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
     * @param runnable 被增强函数
     */
    public static void enhance(Runnable runnable) {
        try {
            INSTANCE.doBefore();
            runnable.run();
        } finally {
            INSTANCE.doAfter();
        }
    }

    /**
     * 增强处理，Consumer 结束时会恢复 MapThreadLocalAdaptor
     *
     * @param consumer 被增强函数
     * @param t        被增强函数输入值
     */
    public static <T> void enhance(Consumer<T> consumer, T t) {
        try {
            INSTANCE.doBefore();
            consumer.accept(t);
        } finally {
            INSTANCE.doAfter();
        }
    }

    /**
     * 增强处理，Supplier 结束时会恢复 MapThreadLocalAdaptor
     *
     * @param supplier 被增强函数
     */
    public static <T> T enhance(Supplier<T> supplier) {
        try {
            INSTANCE.doBefore();
            return supplier.get();
        } finally {
            INSTANCE.doAfter();
        }
    }

    /**
     * 增强处理，Function 结束时会恢复 MapThreadLocalAdaptor
     *
     * @param function 被增强函数
     * @param t        被增强函数输入值
     */
    public static <T, R> R enhance(Function<T, R> function, T t) {
        try {
            INSTANCE.doBefore();
            return function.apply(t);
        } finally {
            INSTANCE.doAfter();
        }
    }

}
