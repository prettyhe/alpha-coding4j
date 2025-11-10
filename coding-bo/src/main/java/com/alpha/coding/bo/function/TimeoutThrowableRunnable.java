package com.alpha.coding.bo.function;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * TimeoutThrowableRunnable
 *
 * @version 1.0
 * @date 2025年04月23日
 */
public class TimeoutThrowableRunnable extends TimeoutThrowableFunction<Object, Object> implements Runnable,
        ThrowableConsumer<Object> {

    TimeoutThrowableRunnable(Runnable target, long timeout, TimeUnit timeUnit) {
        super(ThrowableFunction.of(Objects.requireNonNull(target)), timeout, timeUnit);
    }

    public static TimeoutThrowableRunnable of(Runnable target, long timeout, TimeUnit timeUnit) {
        return new TimeoutThrowableRunnable(target, timeout, timeUnit);
    }

    @Override
    public void run() {
        try {
            getTarget().apply(null);
        } catch (Throwable e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    public void accept(Object o) throws Throwable {
        super.apply(null);
    }

}
