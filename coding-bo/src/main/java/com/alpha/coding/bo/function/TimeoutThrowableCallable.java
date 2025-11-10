package com.alpha.coding.bo.function;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * TimeoutThrowableCallable
 *
 * @version 1.0
 * @date 2025年04月23日
 */
public class TimeoutThrowableCallable<V> extends TimeoutThrowableFunction<Object, V> implements
        java.util.concurrent.Callable<V>, ThrowableSupplier<V> {

    TimeoutThrowableCallable(java.util.concurrent.Callable<V> target, long timeout, TimeUnit timeUnit) {
        super(ThrowableFunction.of(Objects.requireNonNull(target)), timeout, timeUnit);
    }

    public static <V> TimeoutThrowableCallable<V> of(java.util.concurrent.Callable<V> target,
                                                     long timeout, TimeUnit timeUnit) {
        return new TimeoutThrowableCallable<>(target, timeout, timeUnit);
    }

    @Override
    public V call() throws Exception {
        try {
            return super.apply(null);
        } catch (Throwable e) {
            if (e instanceof Exception) {
                throw (Exception) e;
            }
            throw new Exception(e);
        }
    }

    @Override
    public V get() throws Throwable {
        return super.apply(null);
    }

}
