package com.alpha.coding.common.mybatis.dynamic;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;

/**
 * CustomColumn 自定义 BindableColumn
 *
 * @version 1.0
 * Date: 2020/4/18
 * @see org.mybatis.dynamic.sql.Constant
 */
public class CustomColumn<T> implements BindableColumn<T> {

    private final String name;
    private final String alias;

    private CustomColumn(String name) {
        this(name, null);
    }

    private CustomColumn(String name, String alias) {
        this.name = Objects.requireNonNull(name, "column name must not null!");
        this.alias = alias;
    }

    @Override
    public Optional<String> alias() {
        return Optional.ofNullable(alias);
    }

    @Override
    public BindableColumn<T> as(String alias) {
        return new CustomColumn<>(name, alias);
    }

    @Override
    public FragmentAndParameters render(RenderingContext renderingContext) {
        return FragmentAndParameters.fromFragment(name);
    }

    public static <T> CustomColumn<T> of(String name) {
        return new CustomColumn<>(name);
    }

    public static CustomColumn<Byte> ofByte(String name) {
        return of(name);
    }

    public static CustomColumn<Short> ofShort(String name) {
        return of(name);
    }

    public static CustomColumn<Integer> ofInt(String name) {
        return of(name);
    }

    public static CustomColumn<Long> ofLong(String name) {
        return of(name);
    }

    public static CustomColumn<BigDecimal> ofBigDecimal(String name) {
        return of(name);
    }

    public static CustomColumn<String> ofString(String name) {
        return of(name);
    }

    public static CustomColumn<Date> ofDate(String name) {
        return of(name);
    }

}
