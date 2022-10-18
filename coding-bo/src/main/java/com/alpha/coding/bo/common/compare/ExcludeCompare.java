package com.alpha.coding.bo.common.compare;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * ExcludeCompare
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class ExcludeCompare implements ConditionCompare {

    @Override
    public int type() {
        return ConditionType.EXCLUDE.getType();
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
            return CompareResult.PASS;
        }
        if (input == null) {
            return CompareResult.UNKNOWN;
        }
        Set excludes = new HashSet((Collection) benchmark);
        return excludes.contains(function == null ? input : function.apply(input)) ? CompareResult.FAIL
                : CompareResult.PASS;
    }
}
