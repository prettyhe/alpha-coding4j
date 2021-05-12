package com.alpha.coding.bo.function.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.alpha.coding.bo.function.Counter;

/**
 * LocalCounter
 *
 * @version 1.0
 * Date: 2021/5/12
 */
public class LocalCounter implements Counter {

    private ConcurrentHashMap<String, AtomicLong> countMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Long> expireMap = new ConcurrentHashMap<>();

    @Override
    public Long obtain(String key) {
        final AtomicLong count = countMap.get(key);
        if (count == null) {
            return null;
        }
        final Long expire = expireMap.get(key);
        if (expire != null && expire < System.nanoTime()) {
            countMap.remove(key);
            return null;
        }
        return count.get();
    }

    @Override
    public Long incr(String key, long delta) {
        return countMap.computeIfAbsent(key, k -> new AtomicLong(0)).addAndGet(delta);
    }

    @Override
    public void expire(String key, long timeout, TimeUnit unit) {
        if (timeout == -1) {
            expireMap.remove(key);
        } else {
            expireMap.put(key, System.nanoTime() + unit.toNanos(timeout));
        }
    }
}
