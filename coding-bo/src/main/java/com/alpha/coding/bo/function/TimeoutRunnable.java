package com.alpha.coding.bo.function;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * TimeoutRunnable
 *
 * @version 1.0
 * @date 2025年04月23日
 */
public class TimeoutRunnable extends TimeoutConsumer<Object> implements Runnable {

    TimeoutRunnable(Runnable runnable, long timeout, TimeUnit timeUnit) {
        super(t -> runnable.run(), timeout, timeUnit);
        Objects.requireNonNull(runnable);
    }

    public static TimeoutRunnable of(Runnable runnable, long timeout, TimeUnit timeUnit) {
        return new TimeoutRunnable(runnable, timeout, timeUnit);
    }

    @Override
    public void run() {
        super.accept(null);
    }

}
