package com.alpha.coding.common.mybatis.dynamic;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import org.mybatis.dynamic.sql.SqlBuilder;
import org.mybatis.dynamic.sql.where.WhereApplier;
import org.mybatis.dynamic.sql.where.condition.IsLike;

import com.alpha.coding.common.utils.StringUtils;

/**
 * SqlBuilderExtension
 *
 * @version 1.0
 * Date: 2020/8/21
 */
public interface SqlBuilderExtension {

    WhereApplier EMPTY_WHERE_APPLIER = dsl -> {
    };

    static WhereApplier emptyWhereApplier() {
        return EMPTY_WHERE_APPLIER;
    }

    static WhereApplier compose(WhereApplier... whereAppliers) {
        return dsl -> Arrays.stream(whereAppliers).filter(Objects::nonNull).forEach(w -> w.accept(dsl));
    }

    static IsLike<String> isFullLikeWhenPresent(String value) {
        return SqlBuilder.isLikeWhenPresent(() -> StringUtils.isBlank(value) ? null : ("%" + value.trim() + "%"));
    }

    static IsLike<String> isPrefixLikeWhenPresent(String value) {
        return SqlBuilder.isLikeWhenPresent(() -> StringUtils.isBlank(value) ? null : (value.trim() + "%"));
    }

    /**
     * 构建 IsIn 条件，集合为null时忽略，为空时阻断
     *
     * @param values 集合值：如果为null则忽略该条件，即该条件恒定通过；如果为空则阻断该条件，即该条件不通过
     */
    static <T> IsInSafely<T> isInNullIgnoredAndEmptyBlocked(Collection<T> values) {
        return values == null ? IsInSafely.empty(IsInSafely.EmptyStrategy.IGNORE) :
                IsInSafely.of(IsInSafely.EmptyStrategy.BLOCK, values);
    }

    /**
     * 构建 IsIn 条件，集合为null或空时都忽略
     *
     * @param values 集合值：为null或空时都忽略，即该条件恒定通过
     */
    static <T> IsInSafely<T> isInNullOrEmptyBothIgnored(Collection<T> values) {
        return IsInSafely.of(IsInSafely.EmptyStrategy.IGNORE, values);
    }

    /**
     * 构建 IsIn 条件，集合为null或空时都阻断
     *
     * @param values 集合值：为null或空时都阻断，即该条件不通过
     */
    static <T> IsInSafely<T> isInNullOrEmptyBothBlocked(Collection<T> values) {
        return IsInSafely.of(IsInSafely.EmptyStrategy.BLOCK, values);
    }

}
