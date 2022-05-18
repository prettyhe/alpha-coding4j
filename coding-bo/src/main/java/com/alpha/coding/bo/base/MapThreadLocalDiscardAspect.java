package com.alpha.coding.bo.base;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
            final Map<String, Object> existMap = new HashMap<>(discardKeys.length * 2);
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

}
