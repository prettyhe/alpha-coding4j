package com.alpha.coding.bo.base;

import java.util.Map;
import java.util.function.Supplier;

/**
 * MapBuilder
 *
 * @version 1.0
 * Date: 2020/5/8
 */
public class MapBuilder<K, V> {

    private final Map<K, V> map;

    private MapBuilder(Map<K, V> map) {
        this.map = map;
    }

    public static <K, V> MapBuilder<K, V> of(Map<K, V> map) {
        return new MapBuilder<>(map);
    }

    public static <K, V> MapBuilder<K, V> of(Supplier<Map<K, V>> supplier) {
        return new MapBuilder<>(supplier.get());
    }

    public MapBuilder<K, V> fluentPut(K k, V v) {
        this.map.put(k, v);
        return this;
    }

    public MapBuilder<K, V> putAll(Map<? extends K, ? extends V> m) {
        this.map.putAll(m);
        return this;
    }

    public Map<K, V> build() {
        return map;
    }

}
