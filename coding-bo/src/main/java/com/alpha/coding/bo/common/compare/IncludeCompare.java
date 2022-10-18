package com.alpha.coding.bo.common.compare;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * IncludeCompare
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class IncludeCompare implements ConditionCompare {

    @Override
    public int type() {
        return ConditionType.INCLUDE.getType();
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public CompareResult compare(Object input, Object benchmark, Function function) {
        if (benchmark == null) {
            return CompareResult.PASS;
        }
        if (!(benchmark instanceof Collection)) {
            throw new IllegalArgumentException("benchmark not Collection");
        }
        if (((Collection) benchmark).size() == 0) {
            return input == null ? CompareResult.PASS : CompareResult.FAIL;
        }
        if (input == null) {
            return CompareResult.UNKNOWN;
        }
        Set includes = new HashSet((Collection) benchmark);
        return includes.contains(function == null ? input : function.apply(input)) ? CompareResult.PASS
                : CompareResult.FAIL;
    }
}
