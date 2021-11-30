package com.alpha.coding.common.mybatis.dynamic;

import java.util.function.Function;

import org.mybatis.dynamic.sql.AbstractNoValueCondition;

/**
 * IsIdentity
 *
 * @version 1.0
 * Date: 2020/4/18
 */
public class IsIdentity<T> extends AbstractNoValueCondition<T> {

    private Function<String, String> function = Function.identity();

    public IsIdentity() {
    }

    public IsIdentity(Function<String, String> function) {
        this.function = function;
    }

    @Override
    public String renderCondition(String columnName) {
        return function.apply(columnName);
    }

    public static <T> IsIdentity<T> of(Function<String, String> function) {
        return new IsIdentity<>(function);
    }

}
