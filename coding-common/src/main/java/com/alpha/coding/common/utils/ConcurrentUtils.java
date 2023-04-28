package com.alpha.coding.common.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

import com.alpha.coding.bo.base.Tuple;
import com.alpha.coding.bo.function.ThrowableFunction;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

/**
 * ConcurrentUtils
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class ConcurrentUtils {

    public interface RunCallback<T> {
        void execute(T t);
    }

    /**
     * 批量执行，集合每个元素视为单独任务
     */
    public static <T> void batch(Executor executor, Collection<T> ts, final RunCallback<T> callback) {
        if (ts == null || ts.size() == 0) {
            return;
        }
        if (ts.size() == 1) {
            callback.execute(ts.iterator().next());
            return;
        }
        final CountDownLatch countDownLatch = new CountDownLatch(ts.size());
        for (final T t : ts) {
            executor.execute(() -> {
                final long st = System.currentTimeMillis();
                try {
                    callback.execute(t);
                } finally {
                    log.debug("run-callback-cost {}ms", (System.currentTimeMillis() - st));
                    countDownLatch.countDown();
                }
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 批量执行(允许返回结果)，集合每个元素视为单独任务
     */
    public static <T, V> List<Tuple<V, Throwable>> batchCall(Executor executor, Collection<T> ts,
                                                             final ThrowableFunction<T, V> function) {
        if (ts == null || ts.size() == 0) {
            return Collections.emptyList();
        }
        if (ts.size() == 1) {
            final Tuple<V, Throwable> tuple = Tuple.empty();
            try {
                final V v = function.apply(ts.iterator().next());
                tuple.setF(v);
            } catch (Throwable e) {
                tuple.setS(e);
            }
            return Collections.singletonList(tuple);
        }
        final List<Tuple<V, Throwable>> resultList = new ArrayList<>(ts.size());
        final CountDownLatch countDownLatch = new CountDownLatch(ts.size());
        for (final T t : ts) {
            final Tuple<V, Throwable> tuple = Tuple.empty();
            resultList.add(tuple);
            executor.execute(() -> {
                final long st = System.currentTimeMillis();
                try {
                    final V v = function.apply(t);
                    tuple.setF(v);
                } catch (Throwable throwable) {
                    tuple.setS(throwable);
                } finally {
                    log.debug("run-function-cost {}ms", (System.currentTimeMillis() - st));
                    countDownLatch.countDown();
                }
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return resultList;
    }

    /**
     * 分批执行，按执行器数量拆分批次，每批次作为一组到单独任务执行
     */
    public static <T> void batch(Executor executor, int exeSize, Collection<T> ts,
                                 final RunCallback<List<T>> callback) {
        if (ts == null || ts.size() == 0) {
            return;
        }
        List<T> originList = ts instanceof List ? (List<T>) ts : Lists.newArrayList(ts);
        if (executor == null || exeSize <= 0) {
            callback.execute(originList);
            return;
        }
        int cap = ts.size() % exeSize == 0 ? ts.size() / exeSize : (ts.size() / exeSize + 1);
        batch(executor, Lists.partition(originList, cap), callback);
    }

    /**
     * 分批执行(允许返回结果)，按执行器数量拆分批次，每批次作为一组到单独任务执行
     */
    public static <T, V> List<Tuple<List<V>, Throwable>> batchCall(Executor executor, int exeSize, Collection<T> ts,
                                                                   final ThrowableFunction<List<T>, List<V>> function) {
        if (ts == null || ts.size() == 0) {
            return Collections.emptyList();
        }
        List<T> originList = ts instanceof List ? (List<T>) ts : Lists.newArrayList(ts);
        if (executor == null || exeSize <= 0) {
            final Tuple<List<V>, Throwable> tuple = Tuple.empty();
            try {
                List<V> vs = function.apply(originList);
                tuple.setF(vs);
            } catch (Throwable e) {
                tuple.setS(e);
            }
            return Collections.singletonList(tuple);
        }
        int cap = ts.size() % exeSize == 0 ? ts.size() / exeSize : (ts.size() / exeSize + 1);
        return batchCall(executor, Lists.partition(originList, cap), function);
    }

    /**
     * 批量执行，集合每个元素视为单独任务
     */
    public static <T> void join(Executor executor, final Collection<T> ts, final RunCallback<T> callback) {
        if (ts == null || ts.size() == 0) {
            return;
        }
        if (ts.size() == 1) {
            callback.execute(ts.iterator().next());
            return;
        }
        final CountDownLatch countDownLatch = new CountDownLatch(ts.size() - 1);
        final Iterator<T> iterator = ts.iterator();
        final T first = iterator.next();
        for (int i = 1; i < ts.size(); i++) {
            final T next = iterator.next();
            executor.execute(() -> {
                final long st = System.currentTimeMillis();
                try {
                    callback.execute(next);
                } finally {
                    log.debug("run-callback-cost {}ms", (System.currentTimeMillis() - st));
                    countDownLatch.countDown();
                }
            });
        }
        callback.execute(first);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 批量执行，集合每个元素视为单独任务
     */
    public static <T, V> List<Tuple<V, Throwable>> joinCall(Executor executor, final Collection<T> ts,
                                                            final ThrowableFunction<T, V> function) {
        if (ts == null || ts.size() == 0) {
            return Collections.emptyList();
        }
        if (ts.size() == 1) {
            final Tuple<V, Throwable> tuple = Tuple.empty();
            try {
                final V v = function.apply(ts.iterator().next());
                tuple.setF(v);
            } catch (Throwable e) {
                tuple.setS(e);
            }
            return Collections.singletonList(tuple);
        }
        final List<Tuple<V, Throwable>> resultList = new ArrayList<>(ts.size());
        final Tuple<V, Throwable> firstResult = Tuple.empty();
        resultList.add(firstResult);
        final CountDownLatch countDownLatch = new CountDownLatch(ts.size() - 1);
        final Iterator<T> iterator = ts.iterator();
        final T first = iterator.next();
        for (int i = 1; i < ts.size(); i++) {
            final T next = iterator.next();
            final Tuple<V, Throwable> tuple = Tuple.empty();
            resultList.add(tuple);
            executor.execute(() -> {
                final long st = System.currentTimeMillis();
                try {
                    final V v = function.apply(next);
                    tuple.setF(v);
                } catch (Throwable throwable) {
                    tuple.setS(throwable);
                } finally {
                    log.debug("run-function-cost {}ms", (System.currentTimeMillis() - st));
                    countDownLatch.countDown();
                }
            });
        }
        try {
            final V v = function.apply(first);
            firstResult.setF(v);
        } catch (Throwable e) {
            firstResult.setS(e);
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return resultList;
    }

}
