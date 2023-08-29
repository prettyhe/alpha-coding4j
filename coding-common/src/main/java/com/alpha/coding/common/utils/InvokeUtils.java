package com.alpha.coding.common.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.alpha.coding.bo.function.ThrowableSupplier;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * InvokeUtils
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class InvokeUtils {

    private static final ConcurrentHashMap<String, InvokeLock> LOCK_CACHE = new ConcurrentHashMap<>(64);

    @Data
    @Accessors(chain = true)
    public static class InvokeLock {

        private volatile CountDownLatch signal;
        private volatile Thread loaderThread;
        private volatile Object value;

    }

    @Data
    @Accessors(chain = true)
    public static class InvokeResult {
        /**
         * 是否成功获取到锁
         */
        private boolean winLock;
        /**
         * 是否等待被打断
         */
        private boolean interrupted;
        /**
         * 是否等待超时
         */
        private boolean waitTimeout;
        /**
         * 结果
         */
        private Object data;
    }

    /**
     * 同步加载缓存数据
     *
     * @param lockCache                   key锁缓存
     * @param key                         缓存key
     * @param awaitMillis                 最大等待时长
     * @param failFastWhenTimeout         是否等待超时后快速失败
     * @param failFastWhenWaitInterrupted 是否等待中断后快速失败
     * @param valueSupplier               加载器：获取值
     * @param valueConsumer               放置器：值写入缓存
     * @return InvokeResult
     */
    public static InvokeResult syncInvoke(Map<String, InvokeLock> lockCache, String key, Long awaitMillis,
                                          boolean failFastWhenTimeout, boolean failFastWhenWaitInterrupted,
                                          ThrowableSupplier valueSupplier, Consumer valueConsumer) throws Throwable {
        if (key == null) {
            return null;
        }
        boolean create = false;
        InvokeLock lock = lockCache.get(key);
        if (lock == null) {
            synchronized(lockCache) {
                lock = lockCache.get(key);
                if (lock == null) {
                    lock = new InvokeLock();
                    lock.loaderThread = Thread.currentThread();
                    lock.signal = new CountDownLatch(1);
                    create = true;
                    lockCache.put(key, lock);
                }
            }
        }
        if (create || lock.getLoaderThread() == Thread.currentThread()) {
            try {
                Object result = valueSupplier.get();
                lock.value = result;
                if (valueConsumer != null) {
                    valueConsumer.accept(result);
                }
                return new InvokeResult().setWinLock(true).setData(result);
            } finally {
                lock.signal.countDown();
                if (create) {
                    lockCache.remove(key);
                }
            }
        } else {
            try {
                if (awaitMillis == null || awaitMillis < 0) {
                    lock.signal.await();
                } else {
                    final boolean ok = lock.signal.await(awaitMillis, TimeUnit.MILLISECONDS);
                    if (!ok) {
                        log.warn("invoke wait timeout:{} for {}", awaitMillis, key);
                        if (failFastWhenTimeout) {
                            return new InvokeResult().setWaitTimeout(true).setData(lock.value);
                        }
                        Object result = valueSupplier.get();
                        if (valueConsumer != null) {
                            valueConsumer.accept(result);
                        }
                        return new InvokeResult().setWaitTimeout(true).setData(result);
                    }
                }
            } catch (InterruptedException e) {
                log.warn("invoke wait interrupted for {}", key);
                if (failFastWhenWaitInterrupted) {
                    return new InvokeResult().setInterrupted(true).setData(lock.value);
                }
                Object result = valueSupplier.get();
                if (valueConsumer != null) {
                    valueConsumer.accept(result);
                }
                return new InvokeResult().setInterrupted(true).setData(result);
            }
            return new InvokeResult().setData(lock.value);
        }
    }

    /**
     * 同步加载缓存数据
     *
     * @param key                         缓存key
     * @param awaitMillis                 最大等待时长
     * @param failFastWhenTimeout         是否等待超时后快速失败
     * @param failFastWhenWaitInterrupted 是否等待中断后快速失败
     * @param valueSupplier               加载器：获取值
     * @param valueConsumer               放置器：值写入缓存
     * @return InvokeResult
     */
    public static InvokeResult syncInvoke(String key, Long awaitMillis,
                                          boolean failFastWhenTimeout, boolean failFastWhenWaitInterrupted,
                                          ThrowableSupplier valueSupplier, Consumer valueConsumer) throws Throwable {
        return syncInvoke(LOCK_CACHE, key, awaitMillis, failFastWhenTimeout, failFastWhenWaitInterrupted,
                valueSupplier, valueConsumer);
    }

    /**
     * 同步加载缓存数据
     *
     * @param key           缓存key
     * @param awaitMillis   最大等待时长
     * @param valueSupplier 加载器：获取值
     * @param valueConsumer 放置器：值写入缓存
     * @return InvokeResult
     */
    public static InvokeResult syncInvoke(String key, Long awaitMillis,
                                          ThrowableSupplier valueSupplier, Consumer valueConsumer) throws Throwable {
        return syncInvoke(LOCK_CACHE, key, awaitMillis, false, false, valueSupplier, valueConsumer);
    }

}
