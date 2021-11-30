package com.alpha.coding.common.mybatis.dynamic;

import java.util.List;
import java.util.function.Function;

import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.VisitableCondition;
import org.mybatis.dynamic.sql.where.WhereApplier;
import org.mybatis.dynamic.sql.where.condition.IsLikeWhenPresent;

import com.alpha.coding.common.utils.StringUtils;

/**
 * SqlBuilderExtension
 *
 * @version 1.0
 * Date: 2020/8/21
 */
public interface SqlBuilderExtension {

    static SqlCriterion<?> sqlCriterion(String connector) {
        return sqlCriterion(connector, CustomColumn.of(""), IsIdentity.of(Function.identity()));
    }

    static <T> SqlCriterion<T> sqlCriterion(String connector, BindableColumn<T> column,
                                            VisitableCondition<T> condition) {
        return new SqlCriterion.Builder<T>()
                .withColumn(column)
                .withConnector(connector)
                .withCondition(condition)
                .build();
    }

    static <T> SqlCriterion<T> sqlCriterion(String connector, BindableColumn<T> column,
                                            VisitableCondition<T> condition, List<SqlCriterion<?>> subCriteria) {
        return new SqlCriterion.Builder<T>()
                .withColumn(column)
                .withConnector(connector)
                .withCondition(condition)
                .withSubCriteria(subCriteria)
                .build();
    }

    static <T> SqlCriterion.Builder<T> sqlCriterionBuilder(String connector, BindableColumn<T> column) {
        return new SqlCriterion.Builder<T>().withColumn(column).withConnector(connector);
    }

    static WhereApplier compose(WhereApplier w1, WhereApplier w2) {
        return dsl -> dsl.applyWhere(w1).applyWhere(w2);
    }

    static IsLikeWhenPresent<String> isFullLikeWhenPresent(String value) {
        return SqlBuilder.isLikeWhenPresent(() -> StringUtils.isBlank(value) ? null : ("%" + value.trim() + "%"));
    }

    static IsLikeWhenPresent<String> isPrefixLikeWhenPresent(String value) {
        return SqlBuilder.isLikeWhenPresent(() -> StringUtils.isBlank(value) ? null : (value.trim() + "%"));
    }

}
