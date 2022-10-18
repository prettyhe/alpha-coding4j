package com.alpha.coding.bo.common.compare;

import java.util.function.Function;

/**
 * ExistCompare
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class ExistCompare implements ConditionCompare {

    @Override
    public int type() {
        return ConditionType.EXIST.getType();
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public CompareResult compare(Object input, Object benchmark, Function function) {
        if (function != null) {
            return function.apply(input) == null ? CompareResult.FAIL : CompareResult.PASS;
        }
        return input == null ? CompareResult.FAIL : CompareResult.PASS;
    }
}
