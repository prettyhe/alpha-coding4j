package com.alpha.coding.bo.function;

import java.util.Objects;

/**
 * ThrowableFunction
 *
 * @version 1.0
 * Date: 2023/2/15
 */
@FunctionalInterface
public interface ThrowableFunction<T, R> {

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     * @throws Throwable throw Throwable when Exception
     */
    R apply(T t) throws Throwable;

    /**
     * Returns a composed function that first applies the {@code before}
     * function to its input, and then applies this function to the result.
     * If evaluation of either function throws an exception, it is relayed to
     * the caller of the composed function.
     *
     * @param <V>    the type of input to the {@code before} function, and to the
     *               composed function
     * @param before the function to apply before this function is applied
     * @return a composed function that first applies the {@code before}
     * function and then applies this function
     * @throws NullPointerException if before is null
     * @see #andThen(ThrowableFunction)
     */
    default <V> ThrowableFunction<V, R> compose(ThrowableFunction<? super V, ? extends T> before) {
        Objects.requireNonNull(before);
        return (V v) -> apply(before.apply(v));
    }

    /**
     * Returns a composed function that first applies this function to
     * its input, and then applies the {@code after} function to the result.
     * If evaluation of either function throws an exception, it is relayed to
     * the caller of the composed function.
     *
     * @param <V>   the type of output of the {@code after} function, and of the
     *              composed function
     * @param after the function to apply after this function is applied
     * @return a composed function that first applies this function and then
     * applies the {@code after} function
     * @throws NullPointerException if after is null
     * @see #compose(ThrowableFunction)
     */
    default <V> ThrowableFunction<T, V> andThen(ThrowableFunction<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t) -> after.apply(apply(t));
    }

    static <T, V> ThrowableFunction<T, V> of(ThrowableSupplier<V> supplier) {
        return supplier == null ? null : t -> supplier.get();
    }

    static <T, V> ThrowableFunction<T, V> of(ThrowableConsumer<T> consumer) {
        return consumer == null ? null : t -> {
            consumer.accept(t);
            return null;
        };
    }

}
