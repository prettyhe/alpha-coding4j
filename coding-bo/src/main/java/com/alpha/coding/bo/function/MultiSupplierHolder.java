package com.alpha.coding.bo.function;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MultiSupplierHolder
 *
 * @version 1.0
 * Date: 2021/9/14
 */
public class MultiSupplierHolder {

    private final Map<String, SupplierHolder> holderMap = new ConcurrentHashMap<>();

    /**
     * computeIfAbsent
     *
     * @param key    标识
     * @param holder 持有器
     */
    public SupplierHolder computeIfAbsent(String key, SupplierHolder holder) {
        return holderMap.computeIfAbsent(key, k -> holder);
    }

    /**
     * 获取持有器
     *
     * @param key 标识
     */
    public SupplierHolder get(String key) {
        return holderMap.get(key);
    }

    /**
     * 获取值
     *
     * @param key 标识
     */
    public Object getValue(String key) {
        SupplierHolder holder = get(key);
        return holder == null ? null : holder.get();
    }

}
