package com.alpha.coding.bo.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * NamedExecutorPool
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class NamedExecutorPool {

    public static ExecutorService newFixedThreadPool(String poolName, int nThreads) {
        return newFixedThreadPool(poolName, nThreads, 0L);
    }

    public static ExecutorService newFixedThreadPool(String poolName, int nThreads,
                                                     int queueCapacity, RejectedExecutionHandler handler) {
        return newFixedThreadPool(poolName, nThreads, 0L, queueCapacity, handler);
    }

    public static ExecutorService newFixedThreadPool(String poolName, int nThreads, long keepAliveTime) {
        return new ThreadPoolExecutor(nThreads, nThreads, keepAliveTime, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), new NamedThreadFactory(poolName),
                new ThreadPoolExecutor.AbortPolicy());
    }

    public static ExecutorService newFixedThreadPool(String poolName, int nThreads, long keepAliveTime,
                                                     int queueCapacity, RejectedExecutionHandler handler) {
        return new ThreadPoolExecutor(nThreads, nThreads, keepAliveTime, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(queueCapacity), new NamedThreadFactory(poolName), handler);
    }

    public static ScheduledExecutorService newScheduledThreadPool(String poolName, int corePoolSize) {
        return new ScheduledThreadPoolExecutor(corePoolSize, new NamedThreadFactory(poolName));
    }

    public static ScheduledExecutorService newScheduledThreadPool(String poolName, int corePoolSize,
                                                                  RejectedExecutionHandler handler) {
        return new ScheduledThreadPoolExecutor(corePoolSize, new NamedThreadFactory(poolName), handler);
    }

}
