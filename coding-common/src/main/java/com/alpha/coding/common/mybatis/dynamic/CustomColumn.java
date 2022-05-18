package com.alpha.coding.common.mybatis.dynamic;

import java.math.BigDecimal;
import java.sql.JDBCType;
import java.util.Date;
import java.util.Optional;

import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;

/**
 * CustomColumn 自定义BindableColumn
 *
 * @version 1.0
 * Date: 2020/4/18
 */
public class CustomColumn<T> implements BindableColumn<T> {

    private final String name;

    private CustomColumn(String name) {
        this.name = name;
    }

    @Override
    public Optional<String> alias() {
        return Optional.empty();
    }

    @Override
    public BindableColumn<T> as(String alias) {
        return of(alias);
    }

    @Override
    public String renderWithTableAlias(TableAliasCalculator tableAliasCalculator) {
        return name;
    }

    @Override
    public Optional<JDBCType> jdbcType() {
        return Optional.empty();
    }

    @Override
    public Optional<String> typeHandler() {
        return Optional.empty();
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

    public static CustomColumn<String> ofSring(String name) {
        return of(name);
    }

    public static CustomColumn<Date> ofDate(String name) {
        return of(name);
    }

}
