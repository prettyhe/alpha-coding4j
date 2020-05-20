package com.alpha.coding.bo.function;

import java.util.function.Supplier;

/**
 * SupplierHolder
 *
 * @version 1.0
 * @date 2020年04月13日
 */
public class SupplierHolder<T> implements Supplier<T> {

    private volatile T target;
    private final Supplier<T> supplier;

    public SupplierHolder(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T get() {
        if (this.target == null) {
            this.target = this.supplier.get();
        }
        return this.target;
    }

    public Supplier<T> rawSupplier() {
        return this.supplier;
    }

    public T forceGet() {
        return this.supplier.get();
    }
}
