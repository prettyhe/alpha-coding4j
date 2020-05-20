/**
 * Copyright
 */
package com.alpha.coding.bo.config;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import lombok.Data;

/**
 * MapConfig
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Data
public class MapConfig<V> {

    private Class<V> valueClass;

    private Map<String, V> map;

    public V get(String key) {
        return map.get(key);
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    public Collection<V> values() {
        return map.values();
    }

    public Set<Map.Entry<String, V>> entrySet() {
        return map.entrySet();
    }

}
