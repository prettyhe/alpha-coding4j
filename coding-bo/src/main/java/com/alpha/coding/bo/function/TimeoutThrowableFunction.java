package com.alpha.coding.bo.function;

import java.util.concurrent.TimeUnit;

import lombok.Getter;

/**
 * TimeoutThrowableFunction
 *
 * @version 1.0
 * @date 2025年04月23日
 */
@Getter
public class TimeoutThrowableFunction<T, V> implements ThrowableFunction<T, V> {

    private final ThrowableFunction<T, V> target;
    private final long timeout;
    private final TimeUnit timeUnit;

     TimeoutThrowableFunction(ThrowableFunction<T, V> target, long timeout, TimeUnit timeUnit) {
        this.target = target;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }

    public static <T, V> TimeoutThrowableFunction<T, V> of(ThrowableFunction<T, V> target,
                                                           long timeout, TimeUnit timeUnit) {
        return new TimeoutThrowableFunction<>(target, timeout, timeUnit);
    }

    @Override
    public V apply(T t) throws Throwable {
        return target.apply(t);
    }

}
