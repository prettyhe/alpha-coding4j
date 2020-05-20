package com.alpha.coding.bo.function;

/**
 * ThrowableSupplier
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public interface ThrowableSupplier<T> {

    T get() throws Throwable;

}
