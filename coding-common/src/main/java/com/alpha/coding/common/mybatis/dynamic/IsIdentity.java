package com.alpha.coding.common.mybatis.dynamic;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;
import java.util.function.Function;

import org.mybatis.dynamic.sql.AbstractNoValueCondition;

/**
 * IsIdentity
 *
 * @version 1.0
 * Date: 2020/4/18
 */
public class IsIdentity<T> extends AbstractNoValueCondition<T> {

    private final Function<String, String> function;

    public IsIdentity() {
        this(Function.identity());
    }

    public IsIdentity(Function<String, String> function) {
        this.function = Objects.requireNonNull(function, "function must not null!");
    }

    @Override
    public String operator() {
        return function.apply("");
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

    public static IsIdentity<String> ofString() {
        return of();
    }

    public static IsIdentity<Date> ofDate() {
        return of();
    }

}
