package com.alpha.coding.bo.function;

import java.util.Objects;

/**
 * ThrowableConsumer
 *
 * @version 1.0
 * Date: 2023/2/15
 */
@FunctionalInterface
public interface ThrowableConsumer<T> {

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     * @throws Throwable throw Throwable when Exception
     */
    void accept(T t) throws Throwable;

    /**
     * Returns a composed {@code Consumer} that performs, in sequence, this
     * operation followed by the {@code after} operation. If performing either
     * operation throws an exception, it is relayed to the caller of the
     * composed operation.  If performing this operation throws an exception,
     * the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code Consumer} that performs in sequence this
     * operation followed by the {@code after} operation
     */
    default ThrowableConsumer<T> andThen(ThrowableConsumer<? super T> after) {
        Objects.requireNonNull(after);
        return (T t) -> {
            accept(t);
            after.accept(t);
        };
    }

    static <T, V> ThrowableConsumer<T> of(ThrowableFunction<T, V> function) {
        return function == null ? null : function::apply;
    }

}
