package com.alpha.coding.bo.function.impl;

import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.alpha.coding.bo.function.Counter;
import com.alpha.coding.bo.function.ValidateDelegate;

/**
 * TimeWindowValidator
 *
 * @version 1.0
 * Date: 2021/5/12
 */
public class TimeWindowValidator implements ValidateDelegate {

    private final String key;
    private long window = -1;
    private long threshold = -1;
    private final Counter counter;
    private final Consumer<Long> exceedHandler;
    private Predicate<Boolean> countCondition = t -> t != null && !t;

    public TimeWindowValidator(String key, Counter counter, Consumer<Long> exceedHandler) {
        this.key = key;
        this.counter = counter;
        this.exceedHandler = exceedHandler;
    }

    public TimeWindowValidator(String key, long threshold, Counter counter,
                               Consumer<Long> exceedHandler) {
        this.key = key;
        this.threshold = threshold;
        this.counter = counter;
        this.exceedHandler = exceedHandler;
    }

    public TimeWindowValidator(String key, long window, long threshold, Counter counter,
                               Consumer<Long> exceedHandler) {
        this.key = key;
        this.window = window;
        this.threshold = threshold;
        this.counter = counter;
        this.exceedHandler = exceedHandler;
    }

    public TimeWindowValidator(String key, long window, long threshold, Counter counter,
                               Consumer<Long> exceedHandler, Predicate<Boolean> countCondition) {
        this.key = key;
        this.window = window;
        this.threshold = threshold;
        this.counter = counter;
        this.exceedHandler = exceedHandler;
        this.countCondition = countCondition;
    }

    @Override
    public boolean delegate(BooleanSupplier supplier) {
        if (threshold > 0) {
            final Long val = counter.obtain(key);
            if (val != null && val >= threshold) {
                exceedHandler.accept(val);
            }
            final long st = System.nanoTime();
            final boolean result = supplier.getAsBoolean();
            final long elapse = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - st); // 耗时
            if (countCondition.test(result)) {
                counter.incr(key, 1);
                if (val == null || elapse > window) {
                    counter.expire(key, window, TimeUnit.SECONDS);
                }
            }
            return result;
        } else {
            return supplier.getAsBoolean();
        }
    }
}
