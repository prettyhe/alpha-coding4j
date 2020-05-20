package com.alpha.coding.common.function;

import java.util.function.Function;

import javax.annotation.Nullable;

/**
 * FunctionConverter
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class FunctionConverter {

    public static <T, R> com.google.common.base.Function<T, R> to(final Function<T, R> function) {
        return new com.google.common.base.Function<T, R>() {
            @Nullable
            @Override
            public R apply(@Nullable T input) {
                return function.apply(input);
            }
        };
    }

}
