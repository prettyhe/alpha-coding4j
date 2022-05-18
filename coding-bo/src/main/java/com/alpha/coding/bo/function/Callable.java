package com.alpha.coding.bo.function;

/**
 * Callable
 *
 * @version 1.0
 * Date: 2022/5/12
 */
public interface Callable<V> {

    /**
     * Computes a result
     *
     * @return computed result
     */
    V call();

}
