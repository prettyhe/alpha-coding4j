package com.alpha.coding.bo.function;

import java.util.function.Function;
import java.util.function.Supplier;

import lombok.extern.slf4j.Slf4j;

/**
 * OneTimeSupplierHolder
 *
 * @version 1.0
 * Date: 2021/8/16
 */
@Slf4j
public class OneTimeSupplierHolder<T> extends SupplierHolder<T> {

    private volatile boolean done;

    public OneTimeSupplierHolder(Supplier<T> supplier) {
        super(supplier);
    }

    public OneTimeSupplierHolder(String tag, Supplier<T> supplier) {
        super(tag, supplier);
    }

    public OneTimeSupplierHolder(String tag, Supplier<T> supplier,
                                 Function<Object, String> logText) {
        super(tag, supplier, logText);
    }

    @Override
    public T get() {
        if (!done) {
            replace(super.rawSupplier());
            done = true;
        }
        return super.getCurrent();
    }

}
