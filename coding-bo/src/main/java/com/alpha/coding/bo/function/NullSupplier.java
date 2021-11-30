package com.alpha.coding.bo.function;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * NullSupplier
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class NullSupplier<T> implements Supplier<T> {

    private static NullSupplier<?> EMPTY = null;

    private final Runnable runnable;

    public NullSupplier(Runnable runnable) {
        this.runnable = runnable;
    }

    public static <T> NullSupplier<T> empty() {
        if (EMPTY == null) {
            EMPTY = new NullSupplier<>(null);
        }
        return (NullSupplier<T>) EMPTY;
    }

    public static <T> NullSupplier<T> of(Runnable runnable) {
        return new NullSupplier<>(runnable);
    }

    @Override
    public T get() {
        Optional.ofNullable(runnable).ifPresent(Runnable::run);
        return (T) null;
    }

}
