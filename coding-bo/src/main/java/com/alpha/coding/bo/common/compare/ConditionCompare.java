package com.alpha.coding.bo.common.compare;

import java.util.Collection;
import java.util.function.Function;

import com.alpha.coding.bo.enums.util.EnumUtils;

/**
 * ConditionCompare
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public interface ConditionCompare {

    /**
     * 类型，参考{ConditionType}枚举
     */
    int type();

    /**
     * 名字，默认{ConditionType}枚举名
     */
    default String name() {
        final ConditionType conditionType = EnumUtils.safeParse(ConditionType.class, type());
        return conditionType == null ? null : String.valueOf(conditionType);
    }

    /**
     * 比较
     *
     * @param input     输入
     * @param benchmark 基准
     * @param function  输入类型转基准类型(元素类型)的函数
     */
    @SuppressWarnings({"rawtypes"})
    CompareResult compare(Object input, Object benchmark, Function function);

    /**
     * 与
     *
     * @param inputs    输入集合
     * @param benchmark 基准
     * @param function  输入类型转基准类型(元素类型)的函数
     */
    @SuppressWarnings({"rawtypes"})
    default CompareResult and(Collection inputs, Object benchmark, Function function) {
        if (inputs == null || inputs.size() == 0) {
            return CompareResult.UNKNOWN;
        }
        CompareResult result = CompareResult.PASS;
        for (Object input : inputs) {
            result = CompareResult.and(result, compare(input, benchmark, function));
        }
        return result;
    }

    /**
     * 或
     *
     * @param inputs    输入集合
     * @param benchmark 基准
     * @param function  输入类型转基准类型(元素类型)的函数
     */
    @SuppressWarnings({"rawtypes"})
    default CompareResult or(Collection inputs, Object benchmark, Function function) {
        if (inputs == null || inputs.size() == 0) {
            return CompareResult.UNKNOWN;
        }
        CompareResult result = CompareResult.FAIL;
        for (Object input : inputs) {
            result = CompareResult.or(result, compare(input, benchmark, function));
        }
        return result;
    }

}
