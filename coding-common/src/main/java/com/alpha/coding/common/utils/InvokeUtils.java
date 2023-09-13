package com.alpha.coding.common.utils;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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

        private volatile BlockingQueue<Thread> queue;
        private volatile AtomicInteger waitCnt;
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
     * 同步请求控制
     *
     * @param lockCache                   资源key锁缓存
     * @param key                         资源key
     * @param concurrency                 并发数(大于1表示支持多并发处理，否则忽略)
     * @param awaitMillis                 最大等待时长
     * @param failFastWhenAcquireFail     是否竞争失败后快速失败
     * @param failFastWhenTimeout         是否等待超时后快速失败
     * @param failFastWhenWaitInterrupted 是否等待中断后快速失败
     * @param valueSupplier               加载器：提供值函数
     * @param valueConsumer               放置器：值后置处理函数
     * @return InvokeResult
     */
    public static InvokeResult syncInvoke(Map<String, InvokeLock> lockCache, String key, int concurrency,
                                          Long awaitMillis, boolean failFastWhenAcquireFail,
                                          boolean failFastWhenTimeout, boolean failFastWhenWaitInterrupted,
                                          ThrowableSupplier valueSupplier, Consumer valueConsumer) throws Throwable {
        if (key == null) {
            return null;
        }
        InvokeLock lock = lockCache.get(key);
        if (lock == null) {
            synchronized(lockCache) {
                lock = lockCache.get(key);
                if (lock == null) {
                    lock = new InvokeLock();
                    lock.queue = new LinkedBlockingDeque<>(Math.max(concurrency, 1));
                    lock.waitCnt = new AtomicInteger(0);
                    lockCache.put(key, lock);
                }
            }
        }
        boolean inQueue = false;
        boolean addToQueue = false;
        // 成功添加到队列的线程或重入(已在队列)时可直接执行，失败若快速失败则直接返回
        // 否则进入等待流程，等待直接结束或等待到期后立即执行(或快速失败)，等待中断处理
        try {
            if ((inQueue = lock.queue.contains(Thread.currentThread()))
                    || ((addToQueue = lock.queue.offer(Thread.currentThread())))) {
                Object result = valueSupplier.get();
                lock.value = result;
                if (valueConsumer != null) {
                    valueConsumer.accept(result);
                }
                return new InvokeResult().setWinLock(true).setData(result);
            } else if (failFastWhenAcquireFail) {
                // 快速失败，直接返回
                return new InvokeResult().setWinLock(false).setData(lock.value);
            } else {
                lock.waitCnt.incrementAndGet();
                try {
                    if (awaitMillis == null || awaitMillis < 0) {
                        lock.queue.put(Thread.currentThread());
                        addToQueue = true;
                    } else {
                        addToQueue = lock.queue.offer(Thread.currentThread(), awaitMillis, TimeUnit.MILLISECONDS);
                    }
                } catch (InterruptedException e) {
                    log.warn("invoke wait interrupted for {}", key);
                    lock.waitCnt.decrementAndGet();
                    if (failFastWhenWaitInterrupted) {
                        return new InvokeResult().setWinLock(false).setInterrupted(true).setData(lock.value);
                    }
                    // 等待中断后直接处理
                    Object result = valueSupplier.get();
                    lock.value = result;
                    if (valueConsumer != null) {
                        valueConsumer.accept(result);
                    }
                    return new InvokeResult().setWinLock(false).setInterrupted(true).setData(result);
                }
                // 等待结束处理
                lock.waitCnt.decrementAndGet();
                if (addToQueue) {
                    // 等待成功
                    Object result = valueSupplier.get();
                    lock.value = result;
                    if (valueConsumer != null) {
                        valueConsumer.accept(result);
                    }
                    return new InvokeResult().setWinLock(true).setData(result);
                } else {
                    // 等待超时
                    log.warn("invoke wait timeout:{} for {}", awaitMillis, key);
                    if (failFastWhenTimeout) {
                        return new InvokeResult().setWaitTimeout(true).setData(lock.value);
                    }
                    // 等待超时后直接处理
                    Object result = valueSupplier.get();
                    lock.value = result;
                    if (valueConsumer != null) {
                        valueConsumer.accept(result);
                    }
                    return new InvokeResult().setWinLock(false).setWaitTimeout(true).setData(result);
                }
            }
        } finally {
            if (addToQueue) {
                lock.queue.remove(Thread.currentThread());
                if (lock.queue.peek() == null && lock.waitCnt.get() <= 0) {
                    lockCache.remove(key);
                }
            }
        }
    }

    /**
     * 同步请求控制
     *
     * @param key                         资源key
     * @param awaitMillis                 最大等待时长
     * @param concurrency                 并发数
     * @param failFastWhenTimeout         是否等待超时后快速失败
     * @param failFastWhenWaitInterrupted 是否等待中断后快速失败
     * @param valueSupplier               加载器：提供值函数
     * @param valueConsumer               放置器：值后置处理函数
     * @return InvokeResult
     */
    public static InvokeResult syncInvoke(String key, int concurrency, Long awaitMillis,
                                          boolean failFastWhenAcquireFail,
                                          boolean failFastWhenTimeout, boolean failFastWhenWaitInterrupted,
                                          ThrowableSupplier valueSupplier, Consumer valueConsumer) throws Throwable {
        return syncInvoke(LOCK_CACHE, key, concurrency, awaitMillis, failFastWhenAcquireFail, failFastWhenTimeout,
                failFastWhenWaitInterrupted, valueSupplier, valueConsumer);
    }

    /**
     * 同步请求控制
     *
     * @param key                         资源key
     * @param awaitMillis                 最大等待时长
     * @param failFastWhenTimeout         是否等待超时后快速失败
     * @param failFastWhenWaitInterrupted 是否等待中断后快速失败
     * @param valueSupplier               加载器：提供值函数
     * @param valueConsumer               放置器：值后置处理函数
     * @return InvokeResult
     */
    public static InvokeResult syncInvoke(String key, Long awaitMillis, boolean failFastWhenAcquireFail,
                                          boolean failFastWhenTimeout, boolean failFastWhenWaitInterrupted,
                                          ThrowableSupplier valueSupplier, Consumer valueConsumer) throws Throwable {
        return syncInvoke(LOCK_CACHE, key, 1, awaitMillis, failFastWhenAcquireFail, failFastWhenTimeout,
                failFastWhenWaitInterrupted, valueSupplier, valueConsumer);
    }

    /**
     * 同步请求控制
     *
     * @param key           资源key
     * @param awaitMillis   最大等待时长
     * @param valueSupplier 加载器：提供值函数
     * @param valueConsumer 放置器：值后置处理函数
     * @return InvokeResult
     */
    public static InvokeResult syncInvoke(String key, Long awaitMillis,
                                          ThrowableSupplier valueSupplier, Consumer valueConsumer) throws Throwable {
        return syncInvoke(LOCK_CACHE, key, 1, awaitMillis, false, false, false, valueSupplier, valueConsumer);
    }

}
