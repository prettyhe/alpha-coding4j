package com.alpha.coding.bo.function;

/**
 * TiFunction
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public interface TiFunction<T, U, P, R> {

    /**
     * Applies this function to the given arguments.
     *
     * @param t the first function argument
     * @param u the second function argument
     * @param p the third input argument
     *
     * @return the function result
     */
    R apply(T t, U u, P p);
}
