package com.alpha.coding.bo.common;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * MapCache
 *
 * @version 1.0
 * Date: 2020/4/9
 */
public class MapCache {

    /**
     * 调度服务
     */
    private static final ScheduledExecutorService SCHEDULED_EXECUTOR = Executors.newSingleThreadScheduledExecutor();
    /**
     * 缓存容器
     */
    private final ConcurrentHashMap<String, CacheValueWrapper> valMap = new ConcurrentHashMap<>();
    /**
     * 锁容器
     */
    private final ConcurrentHashMap<String, InvokeLock> lockCache = new ConcurrentHashMap<>();
    /**
     * 过期时间
     */
    private final long expireNanos;

    public MapCache(long expireMillis) {
        this.expireNanos = TimeUnit.MILLISECONDS.toNanos(expireMillis);
        final long expireInterval = TimeUnit.SECONDS.toNanos(20) + expireNanos;
        SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> valMap.keySet().stream()
                        .filter(p -> Math.abs(System.nanoTime() - valMap.get(p).freshTimestamp.get()) > expireInterval)
                        .forEach(valMap::remove),
                expireMillis, expireMillis, TimeUnit.MILLISECONDS);
    }

    private static class CacheValueWrapper<T> {
        private final AtomicReference<T> reference = new AtomicReference<>(null);
        private final AtomicLong freshTimestamp = new AtomicLong(0);
    }

    public <T> T computeIfAbsent(String key, Function<String, T> loader) {
        final CacheValueWrapper<T> wrapper = valMap.computeIfAbsent(key, k -> new CacheValueWrapper<T>());
        long delta = System.nanoTime() - wrapper.freshTimestamp.get();
        if (delta <= -expireNanos || delta > expireNanos) {
            wrapper.reference.set(loader.apply(key));
            wrapper.freshTimestamp.set(System.nanoTime());
        }
        return wrapper.reference.get();
    }

    @Data
    @Accessors(chain = true)
    private static class InvokeLock {

        private CountDownLatch signal;
        private Thread loaderThread;
        private Object value;

    }

    /**
     * 标记缓存过期
     */
    public void markCacheExpire(String key) {
        valMap.computeIfAbsent(key, k -> new CacheValueWrapper<>()).freshTimestamp.set(0);
    }

    /**
     * 同步加载
     */
    public <T> T syncComputeIfAbsent(String key, Function<String, T> loader, Long awaitMillis) {
        final CacheValueWrapper<T> wrapper = valMap.computeIfAbsent(key, k -> new CacheValueWrapper<T>());
        long delta = System.nanoTime() - wrapper.freshTimestamp.get();
        if (delta > 0 && delta <= expireNanos) {
            return wrapper.reference.get();
        }
        boolean create = false;
        final InvokeLock[] lock = new InvokeLock[] {lockCache.get(key)};
        if (lock[0] == null) {
            synchronized(lockCache) {
                lock[0] = lockCache.get(key);
                if (lock[0] == null) {
                    lock[0] = new InvokeLock();
                    lock[0].loaderThread = Thread.currentThread();
                    lock[0].signal = new CountDownLatch(1);
                    create = true;
                    lockCache.put(key, lock[0]);
                }
            }
        }
        // 执行load数据逻辑
        final Supplier<T> execSupplier = () -> {
            T result = loader.apply(key);
            wrapper.reference.set(result);
            wrapper.freshTimestamp.set(System.nanoTime());
            lock[0].value = result;
            return result;
        };
        if (create || lock[0].getLoaderThread() == Thread.currentThread()) {
            try {
                return execSupplier.get();
            } finally {
                lock[0].signal.countDown();
                if (create) {
                    lockCache.remove(key);
                }
            }
        } else {
            try {
                if (awaitMillis == null) {
                    lock[0].signal.await();
                } else {
                    final boolean ok = lock[0].signal.await(awaitMillis, TimeUnit.MILLISECONDS);
                    if (!ok) {
                        return execSupplier.get();
                    }
                }
            } catch (InterruptedException e) {
                return execSupplier.get();
            }
            return (T) (lock[0].value);
        }
    }

}
