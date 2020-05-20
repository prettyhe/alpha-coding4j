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

    public Runnable runnable;

    public NullSupplier(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public T get() {
        Optional.ofNullable(runnable).ifPresent(Runnable::run);
        return (T) null;
    }
}
