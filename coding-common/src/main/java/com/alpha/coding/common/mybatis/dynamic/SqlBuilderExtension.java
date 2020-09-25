package com.alpha.coding.common.mybatis.dynamic;

import java.util.List;
import java.util.function.Function;

import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.VisitableCondition;

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

}
