package com.alpha.coding.common.mybatis.dynamic;

import java.sql.JDBCType;
import java.util.Optional;

import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;

/**
 * CustomColumn 自定义BindableColumn
 *
 * @version 1.0
 * Date: 2020/4/18
 */
public class CustomColumn implements BindableColumn {

    private String name;

    private CustomColumn(String name) {
        this.name = name;
    }

    @Override
    public Optional<String> alias() {
        return Optional.empty();
    }

    @Override
    public BindableColumn as(String alias) {
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

    public static CustomColumn of(String name) {
        return new CustomColumn(name);
    }
}
