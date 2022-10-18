package com.alpha.coding.bo.common.compare;

import java.util.function.Function;

/**
 * NonValueCompare
 *
 * @version 1.0
 * Date: 2020/4/21
 */
public class NonValueCompare extends ValueCompare {

    @Override
    public int type() {
        return ConditionType.NON_VALUE.getType();
    }

    @Override
    @SuppressWarnings({"rawtypes"})
    public CompareResult compare(Object input, Object benchmark, Function function) {
        if (benchmark == null && input != null) {
            return CompareResult.PASS;
        } else if (benchmark == null) {
            return CompareResult.FAIL;
        } else if (input == null) {
            return CompareResult.FAIL;
        } else {
            return super.compare(input, benchmark, function).reverse();
        }
    }

}
