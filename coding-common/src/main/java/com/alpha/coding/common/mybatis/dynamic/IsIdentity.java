package com.alpha.coding.common.mybatis.dynamic;

import java.math.BigDecimal;
import java.util.Date;
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

    public static <T> IsIdentity<T> of() {
        return new IsIdentity<>();
    }

    public static IsIdentity<Byte> ofByte() {
        return of();
    }

    public static IsIdentity<Short> ofShort() {
        return of();
    }

    public static IsIdentity<Integer> ofInt() {
        return of();
    }

    public static IsIdentity<Long> ofLong() {
        return of();
    }

    public static IsIdentity<BigDecimal> ofBigDecimal() {
        return of();
    }

    public static IsIdentity<String> ofSring() {
        return of();
    }

    public static IsIdentity<Date> ofDate() {
        return of();
    }

}
