package com.alpha.coding.common.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.alpha.coding.bo.base.Tuple;
import com.alpha.coding.bo.function.ThrowableFunction;
import com.alpha.coding.bo.function.TimeoutConsumer;
import com.alpha.coding.bo.function.TimeoutRunnable;
import com.alpha.coding.bo.function.TimeoutThrowableFunction;
import com.alpha.coding.bo.function.TimeoutThrowableRunnable;
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

    /**
     * 批量执行，集合每个元素视为单独任务待处理数据
     *
     * @param executor 执行器
     * @param ts       待任务处理的数据集合
     * @param handler  任务处理回调
     */
    public static <T> boolean batch(Executor executor, Collection<T> ts, final Consumer<T> handler)
            throws InterruptedException {
        if (ts == null || ts.isEmpty()) {
            return true;
        }
        final CountDownLatch countDownLatch = new CountDownLatch(ts.size());
        for (final T t : ts) {
            executor.execute(() -> {
                final long st = System.currentTimeMillis();
                try {
                    handler.accept(t);
                } finally {
                    if (log.isDebugEnabled()) {
                        log.debug("run-function start at {}, cost {}ms", st, (System.currentTimeMillis() - st));
                    }
                    countDownLatch.countDown();
                }
            });
        }
        if (handler instanceof TimeoutConsumer && ((TimeoutConsumer<T>) handler).getTimeout() > 0) {
            return countDownLatch.await(((TimeoutConsumer<T>) handler).getTimeout(),
                    ((TimeoutConsumer<T>) handler).getTimeUnit());
        } else {
            countDownLatch.await();
            return true;
        }
    }

    /**
     * 分批执行，按执行器数量拆分批次，每批次作为一个单独任务的待处理数据
     *
     * @param executor 执行器
     * @param execSize 执行器数量
     * @param ts       待任务处理的数据集合
     * @param handler  分批任务处理回调
     */
    public static <T> boolean batch(Executor executor, int execSize, Collection<T> ts,
                                    final Consumer<List<T>> handler) throws InterruptedException {
        if (ts == null || ts.isEmpty()) {
            return true;
        }
        List<T> originList = ts instanceof List ? (List<T>) ts : Lists.newArrayList(ts);
        if (executor == null || execSize <= 0) {
            handler.accept(originList);
            return true;
        }
        int cap = ts.size() % execSize == 0 ? ts.size() / execSize : (ts.size() / execSize + 1);
        return batch(executor, Lists.partition(originList, cap), handler);
    }

    /**
     * 函数转换
     */
    private static <T, V> ThrowableFunction<T, V> convertToFunction(Consumer<T> consumer) {
        if (consumer instanceof TimeoutConsumer) {
            return TimeoutThrowableFunction.of(x -> {
                        consumer.accept(x);
                        return null;
                    }, ((TimeoutConsumer<T>) consumer).getTimeout(),
                    ((TimeoutConsumer<T>) consumer).getTimeUnit());
        } else {
            return x -> {
                consumer.accept(x);
                return null;
            };
        }
    }

    /**
     * 批量执行，集合每个元素为数据与任务组合，适用于每个数据处理任务不一样、需要精细化控制的场景
     *
     * @param executor 执行器
     * @param tasks    任务集合
     */
    public static <T> boolean batchConsume(Executor executor, Collection<Tuple<T, Consumer<T>>> tasks)
            throws InterruptedException {
        return tasks == null || batchExecFunction(executor, tasks.stream()
                .map(t -> Tuple.of(t.getF(), convertToFunction(t.getS())))
                .collect(Collectors.toList())).getF();
    }

    /**
     * 批量执行，集合每个元素为数据与任务组合，适用于每个数据处理任务不一样、需要精细化控制的场景
     *
     * @param executor 执行器
     * @param tasks    任务集合
     */
    public static <T, V> Tuple<Boolean, List<Tuple<V, Throwable>>> batchExecFunction(
            Executor executor, Collection<Tuple<T, ThrowableFunction<T, V>>> tasks) throws InterruptedException {
        if (tasks == null || tasks.isEmpty()) {
            return Tuple.of(true, Collections.emptyList());
        }
        long maxTimeoutMillis = -1;
        int timeoutTaskCnt = 0;
        int noTimeoutTaskCnt = 0;
        for (Tuple<T, ThrowableFunction<T, V>> task : tasks) {
            if (task.getS() instanceof TimeoutThrowableFunction) {
                TimeoutThrowableFunction<T, V> function = (TimeoutThrowableFunction<T, V>) task.getS();
                if (function.getTimeout() > 0) {
                    timeoutTaskCnt++;
                    maxTimeoutMillis = Long.max(maxTimeoutMillis,
                            function.getTimeUnit().convert(function.getTimeout(), TimeUnit.MILLISECONDS));
                } else {
                    noTimeoutTaskCnt++;
                }
            } else {
                noTimeoutTaskCnt++;
            }
        }
        final CountDownLatch timeoutCountDownLatch = new CountDownLatch(timeoutTaskCnt);
        final CountDownLatch noTimeoutCountDownLatch = new CountDownLatch(noTimeoutTaskCnt);
        final List<Tuple<V, Throwable>> resultList = new ArrayList<>(tasks.size());
        for (Tuple<T, ThrowableFunction<T, V>> task : tasks) {
            final Tuple<V, Throwable> resultTuple = Tuple.empty();
            resultList.add(resultTuple);
            executor.execute(() -> {
                final long st = System.currentTimeMillis();
                try {
                    final V v = task.getS().apply(task.getF());
                    resultTuple.setF(v);
                } catch (Throwable throwable) {
                    resultTuple.setS(throwable);
                } finally {
                    if (log.isDebugEnabled()) {
                        log.debug("run-function start at {}, cost {}ms", st, (System.currentTimeMillis() - st));
                    }
                    if (task.getS() instanceof TimeoutThrowableFunction
                            && ((TimeoutThrowableFunction<T, V>) task.getS()).getTimeout() > 0) {
                        timeoutCountDownLatch.countDown();
                    } else {
                        noTimeoutCountDownLatch.countDown();
                    }
                }
            });
        }
        final long startAwaitNanos = System.nanoTime();
        // 先等没有超时设置的任务执行完成
        noTimeoutCountDownLatch.await();
        // 再等超时任务设置的最大等待时间
        if (maxTimeoutMillis > 0) {
            final long surplusTimeoutMillis =
                    maxTimeoutMillis - TimeUnit.NANOSECONDS.toMillis(Long.max(0, System.nanoTime() - startAwaitNanos));
            if (surplusTimeoutMillis > 0) {
                return Tuple.of(timeoutCountDownLatch.await(surplusTimeoutMillis, TimeUnit.MILLISECONDS), resultList);
            }
        }
        return Tuple.of(true, resultList);
    }

    /**
     * 批量执行，集合每个元素为数据与任务组合，适用于每个数据处理任务不一样、需要精细化控制的场景
     *
     * @param executor 执行器
     * @param tasks    任务集合
     */
    @SuppressWarnings({"unchecked"})
    public static <T, V> Tuple<Boolean, List<Tuple<V, Throwable>>> batchExecCall(Executor executor,
                                                                                 Collection<Callable<V>> tasks)
            throws InterruptedException {
        if (tasks == null || tasks.isEmpty()) {
            return Tuple.of(true, Collections.emptyList());
        }
        final List<Tuple<T, ThrowableFunction<T, V>>> decorateTasks = new ArrayList<>(tasks.size());
        for (Callable<V> task : tasks) {
            ThrowableFunction<T, V> function = null;
            if (task instanceof ThrowableFunction) {
                function = (ThrowableFunction<T, V>) task;
            } else {
                function = ThrowableFunction.of(task);
            }
            decorateTasks.add(Tuple.of(null, function));
        }
        return batchExecFunction(executor, decorateTasks);
    }

    /**
     * 批量执行，集合每个元素为数据与任务组合，适用于每个数据处理任务不一样、需要精细化控制的场景
     *
     * @param executor 执行器
     * @param tasks    任务集合
     */
    @SuppressWarnings({"unchecked"})
    public static <T, V> Tuple<Boolean, List<Throwable>> batchExecRun(Executor executor, Collection<Runnable> tasks)
            throws InterruptedException {
        if (tasks == null || tasks.isEmpty()) {
            return Tuple.of(true, Collections.emptyList());
        }
        final List<Tuple<T, ThrowableFunction<T, V>>> decorateTasks = new ArrayList<>(tasks.size());
        for (Runnable task : tasks) {
            ThrowableFunction<T, V> function = null;
            if (task instanceof ThrowableFunction) {
                function = (ThrowableFunction<T, V>) task;
            } else if (task instanceof TimeoutRunnable) {
                function = (ThrowableFunction<T, V>) TimeoutThrowableRunnable.of(task,
                        ((TimeoutRunnable) task).getTimeout(),
                        ((TimeoutRunnable) task).getTimeUnit());
            } else {
                function = ThrowableFunction.of(task);
            }
            decorateTasks.add(Tuple.of(null, function));
        }
        final Tuple<Boolean, List<Tuple<V, Throwable>>> resultTuple = batchExecFunction(executor, decorateTasks);
        return Tuple.of(resultTuple.getF(), resultTuple.getS().stream().map(Tuple::getS).collect(Collectors.toList()));
    }

    /**
     * 批量执行(允许返回结果)，集合每个元素视为单独任务待处理数据
     *
     * @param executor 执行器
     * @param ts       待任务处理的数据集合
     * @param function 任务处理回调
     */
    public static <T, V> Tuple<Boolean, List<Tuple<V, Throwable>>> batchCall(Executor executor, Collection<T> ts,
                                                                             final ThrowableFunction<T, V> function)
            throws InterruptedException {
        if (ts == null || ts.isEmpty()) {
            return Tuple.of(true, Collections.emptyList());
        }
        final List<Tuple<V, Throwable>> resultList = new ArrayList<>(ts.size());
        final CountDownLatch countDownLatch = new CountDownLatch(ts.size());
        for (final T t : ts) {
            final Tuple<V, Throwable> resultTuple = Tuple.empty();
            resultList.add(resultTuple);
            executor.execute(() -> {
                final long st = System.currentTimeMillis();
                try {
                    final V v = function.apply(t);
                    resultTuple.setF(v);
                } catch (Throwable throwable) {
                    resultTuple.setS(throwable);
                } finally {
                    if (log.isDebugEnabled()) {
                        log.debug("run-function start at {}, cost {}ms", st, (System.currentTimeMillis() - st));
                    }
                    countDownLatch.countDown();
                }
            });
        }
        if (function instanceof TimeoutThrowableFunction
                && ((TimeoutThrowableFunction<T, V>) function).getTimeout() > 0) {
            TimeoutThrowableFunction<T, V> func = (TimeoutThrowableFunction<T, V>) function;
            return Tuple.of(countDownLatch.await(func.getTimeout(), func.getTimeUnit()), resultList);
        } else {
            countDownLatch.await();
            return Tuple.of(true, resultList);
        }
    }

    /**
     * 分批执行(允许返回结果)，按执行器数量拆分批次，每批次作为一组到单独任务执行
     *
     * @param executor 执行器
     * @param execSize 执行器数量
     * @param ts       待任务处理的数据集合
     * @param function 分批任务处理回调
     */
    public static <T, V> Tuple<Boolean, List<Tuple<List<V>, Throwable>>> batchCall(
            Executor executor, int execSize, Collection<T> ts,
            final ThrowableFunction<List<T>, List<V>> function) throws InterruptedException {
        if (ts == null || ts.isEmpty()) {
            return Tuple.of(true, Collections.emptyList());
        }
        List<T> originList = ts instanceof List ? (List<T>) ts : Lists.newArrayList(ts);
        if (executor == null || execSize <= 0) {
            final Tuple<List<V>, Throwable> tuple = Tuple.empty();
            try {
                List<V> vs = function.apply(originList);
                tuple.setF(vs);
            } catch (Throwable e) {
                tuple.setS(e);
            }
            return Tuple.of(true, Collections.singletonList(tuple));
        }
        int cap = ts.size() % execSize == 0 ? ts.size() / execSize : (ts.size() / execSize + 1);
        return batchCall(executor, Lists.partition(originList, cap), function);
    }

    /**
     * 批量执行，集合每个元素视为单独任务待处理数据，发起线程执行第一个任务，剩余任务交给执行器异步执行
     *
     * @param executor 执行器
     * @param ts       待任务处理的数据集合
     * @param handler  任务处理回调
     * @return 是否执行完成，非超时任务处理则一定完成，否则等待超时时间结束
     */
    public static <T> boolean join(Executor executor, final Collection<T> ts, final Consumer<T> handler)
            throws InterruptedException {
        if (ts == null || ts.isEmpty()) {
            return true;
        }
        return joinCall(executor, ts, convertToFunction(handler)).getF();
    }

    /**
     * 批量执行，集合每个元素视为单独任务待处理数据，发起线程执行第一个任务，剩余任务交给执行器异步执行
     *
     * @param executor 执行器
     * @param ts       待任务处理的数据集合
     * @param function 任务处理回调
     */
    public static <T, V> Tuple<Boolean, List<Tuple<V, Throwable>>> joinCall(Executor executor, final Collection<T> ts,
                                                                            final ThrowableFunction<T, V> function)
            throws InterruptedException {
        if (ts == null || ts.isEmpty()) {
            return Tuple.of(true, Collections.emptyList());
        }
        if (ts.size() == 1) {
            final Tuple<V, Throwable> tuple = Tuple.empty();
            try {
                final V v = function.apply(ts.iterator().next());
                tuple.setF(v);
            } catch (Throwable e) {
                tuple.setS(e);
            }
            return Tuple.of(true, Collections.singletonList(tuple));
        }
        final List<Tuple<V, Throwable>> resultList = new ArrayList<>(ts.size());
        final Tuple<V, Throwable> firstResult = Tuple.empty();
        resultList.add(firstResult);
        final CountDownLatch countDownLatch = new CountDownLatch(ts.size() - 1);
        final Iterator<T> iterator = ts.iterator();
        final T first = iterator.next();
        // 从第一个开始的剩余任务提交到执行器处理
        for (int i = 1; i < ts.size(); i++) {
            final T next = iterator.next();
            final Tuple<V, Throwable> resultTuple = Tuple.empty();
            resultList.add(resultTuple);
            executor.execute(() -> {
                final long st = System.currentTimeMillis();
                try {
                    final V v = function.apply(next);
                    resultTuple.setF(v);
                } catch (Throwable throwable) {
                    resultTuple.setS(throwable);
                } finally {
                    if (log.isDebugEnabled()) {
                        log.debug("run-function start at {}, cost {}ms", st, (System.currentTimeMillis() - st));
                    }
                    countDownLatch.countDown();
                }
            });
        }
        final long startAwaitNanos = System.nanoTime();
        // 第一个任务处理
        try {
            final V v = function.apply(first);
            firstResult.setF(v);
        } catch (Throwable e) {
            firstResult.setS(e);
        }
        // 等待结果
        if (function instanceof TimeoutThrowableFunction
                && ((TimeoutThrowableFunction<T, V>) function).getTimeout() > 0) {
            final TimeoutThrowableFunction<T, V> func = (TimeoutThrowableFunction<T, V>) function;
            final long surplusTimeoutMillis = func.getTimeUnit().toMillis(func.getTimeout())
                    - TimeUnit.NANOSECONDS.toMillis(Long.max(0, System.nanoTime() - startAwaitNanos));
            if (surplusTimeoutMillis > 0) {
                // 等待剩余需要等待的时间
                return Tuple.of(countDownLatch.await(surplusTimeoutMillis, TimeUnit.MILLISECONDS), resultList);
            }
            return Tuple.of(countDownLatch.await(1, TimeUnit.NANOSECONDS), resultList);
        } else {
            countDownLatch.await();
            return Tuple.of(true, resultList);
        }
    }

}
