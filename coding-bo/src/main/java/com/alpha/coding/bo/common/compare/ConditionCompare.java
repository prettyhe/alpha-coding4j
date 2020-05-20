package com.alpha.coding.bo.common.compare;

import java.util.Collection;
import java.util.Iterator;
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
    CompareResult compare(Object input, Object benchmark, Function function);

    /**
     * 与
     *
     * @param inputs    输入集合
     * @param benchmark 基准
     * @param function  输入类型转基准类型(元素类型)的函数
     */
    default CompareResult and(Collection inputs, Object benchmark, Function function) {
        if (inputs == null || inputs.size() == 0) {
            return CompareResult.UNKNOWN;
        }
        CompareResult result = CompareResult.PASS;
        for (Iterator iterator = inputs.iterator(); iterator.hasNext(); ) {
            result = CompareResult.and(result, compare(iterator.next(), benchmark, function));
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
    default CompareResult or(Collection inputs, Object benchmark, Function function) {
        if (inputs == null || inputs.size() == 0) {
            return CompareResult.UNKNOWN;
        }
        CompareResult result = CompareResult.FAIL;
        for (Iterator iterator = inputs.iterator(); iterator.hasNext(); ) {
            result = CompareResult.or(result, compare(iterator.next(), benchmark, function));
        }
        return result;
    }

}
