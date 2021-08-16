package com.alpha.coding.bo.function;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.extern.slf4j.Slf4j;

/**
 * SupplierHolder
 *
 * @version 1.0
 * @date 2020年04月13日
 */
@Slf4j
public class SupplierHolder<T> implements Supplier<T> {

    private volatile T target;
    private final String tag;
    private final Supplier<T> supplier;
    private final Function<Object, String> logText;

    public SupplierHolder(Supplier<T> supplier) {
        this.tag = null;
        this.supplier = supplier;
        this.logText = Objects::toString;
    }

    public SupplierHolder(String tag, Supplier<T> supplier) {
        this.tag = tag;
        this.supplier = supplier;
        this.logText = Objects::toString;
    }

    public SupplierHolder(String tag, Supplier<T> supplier, Function<Object, String> logText) {
        this.tag = tag;
        this.supplier = supplier;
        this.logText = logText;
    }

    @Override
    public T get() {
        if (this.target == null) {
            try {
                this.target = this.supplier.get();
            } finally {
                if (this.tag != null) {
                    log.info("{} => {}", this.tag, this.logText.apply(this.target));
                }
            }
        }
        return this.target;
    }

    public Supplier<T> rawSupplier() {
        return this.supplier;
    }

    public T forceGet() {
        return this.supplier.get();
    }

    public T getCurrent() {
        return this.target;
    }

    public T replace(Supplier<T> supplier) {
        try {
            this.target = supplier.get();
        } finally {
            if (this.tag != null) {
                log.info("{} => {}", this.tag, this.logText.apply(this.target));
            }
        }
        return this.target;
    }

    public T refresh() {
        return replace(this.supplier);
    }

}
