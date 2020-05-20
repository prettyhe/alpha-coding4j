package com.alpha.coding.bo.function;

/**
 * TiConsumer
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public interface TiConsumer<T, U, P> {

    /**
     * Performs this operation on the given arguments.
     *
     * @param t the first input argument
     * @param u the second input argument
     * @param p the third input argument
     */
    void accept(T t, U u, P p);

}
