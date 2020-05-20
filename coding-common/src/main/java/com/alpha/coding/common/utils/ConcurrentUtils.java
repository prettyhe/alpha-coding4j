package com.alpha.coding.common.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

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

    private static class CountDownLatchRunnable implements Runnable {

        private CountDownLatch countDownLatch;
        private Runnable runnable;

        CountDownLatchRunnable(CountDownLatch countDownLatch, Runnable runnable) {
            this.countDownLatch = countDownLatch;
            this.runnable = runnable;
        }

        @Override
        public void run() {
            final long st = System.currentTimeMillis();
            try {
                runnable.run();
            } finally {
                if (log.isDebugEnabled()) {
                    log.debug("run-callback-cost {}ms", (System.currentTimeMillis() - st));
                }
                countDownLatch.countDown();
            }
        }

    }

    public static <T> void batch(Executor executor, Collection<T> ts, final Consumer<T> callback) {
        if (ts == null || ts.size() == 0) {
            return;
        } else if (ts.size() == 1) {
            callback.accept(ts.iterator().next());
            return;
        }
        final CountDownLatch countDownLatch = new CountDownLatch(ts.size());
        for (final T t : ts) {
            executor.execute(new CountDownLatchRunnable(countDownLatch, () -> callback.accept(t)));
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static <T> void batch(Executor executor, int exeSize, Collection<T> ts,
                                 final Consumer<List<T>> callback) {
        if (ts == null || ts.size() == 0) {
            return;
        }
        List<T> originList = ts instanceof List ? (List<T>) ts : Lists.<T>newArrayList(ts);
        if (executor == null || exeSize <= 0) {
            callback.accept(originList);
            return;
        }
        int cap = ts.size() % exeSize == 0 ? ts.size() / exeSize : (ts.size() / exeSize + 1);
        batch(executor, Lists.partition(originList, cap), callback);
    }

    public static <T> void join(Executor executor, final Collection<T> ts, final Consumer<T> callback) {
        if (ts == null || ts.size() == 0) {
            return;
        }
        if (ts.size() == 1) {
            callback.accept(ts.iterator().next());
            return;
        }
        final CountDownLatch countDownLatch = new CountDownLatch(ts.size() - 1);
        final Iterator<T> iterator = ts.iterator();
        final T first = iterator.next();
        for (int i = 1; i < ts.size(); i++) {
            final T next = iterator.next();
            executor.execute(new CountDownLatchRunnable(countDownLatch, () -> callback.accept(next)));
        }
        callback.accept(first);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
