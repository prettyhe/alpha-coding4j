package com.alpha.coding.bo.base;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MapThreadLocalMirrorAspect {

    private static final String ASPECT_VERSION_KEY = "AspectVersion";
    private static final String MIRROR_KEY_PREFIX = "Mirror_";

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
                        return new HashMap<>(parentValue);
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

}
