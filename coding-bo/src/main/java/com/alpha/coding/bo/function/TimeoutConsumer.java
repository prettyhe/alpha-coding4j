package com.alpha.coding.bo.function;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import lombok.Getter;

/**
 * TimeoutConsumer
 *
 * @version 1.0
 * @date 2025年04月23日
 */
@Getter
public class TimeoutConsumer<T> implements Consumer<T> {

    private final Consumer<T> target;
    private final long timeout;
    private final TimeUnit timeUnit;

    TimeoutConsumer(Consumer<T> target, long timeout, TimeUnit timeUnit) {
        this.target = Objects.requireNonNull(target);
        this.timeout = timeout;
        this.timeUnit = Objects.requireNonNull(timeUnit);
    }

    public static <T> TimeoutConsumer<T> of(Consumer<T> target, long timeout, TimeUnit timeUnit) {
        return new TimeoutConsumer<>(target, timeout, timeUnit);
    }

    @Override
    public void accept(T t) {
        this.target.accept(t);
    }

}
