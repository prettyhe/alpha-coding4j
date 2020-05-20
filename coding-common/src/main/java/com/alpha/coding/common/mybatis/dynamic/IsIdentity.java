package com.alpha.coding.common.mybatis.dynamic;

import java.util.Objects;
import java.util.function.Function;

import org.mybatis.dynamic.sql.AbstractNoValueCondition;

/**
 * IsIdentity
 *
 * @version 1.0
 * Date: 2020/4/18
 */
public class IsIdentity extends AbstractNoValueCondition {

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

    public static IsIdentity of(Function<String, String> function) {
        Objects.requireNonNull(function);
        return new IsIdentity(function);
    }

}
