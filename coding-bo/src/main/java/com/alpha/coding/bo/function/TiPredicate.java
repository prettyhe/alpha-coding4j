package com.alpha.coding.bo.function;

/**
 * TiPredicate
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public interface TiPredicate<T, U, P> {

    /**
     * Evaluates this predicate on the given arguments.
     *
     * @param t the first input argument
     * @param u the second input argument
     * @param p the third input argument
     *
     * @return {@code true} if the input arguments match the predicate,
     * otherwise {@code false}
     */
    boolean test(T t, U u, P p);

}
