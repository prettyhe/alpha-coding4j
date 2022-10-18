package com.alpha.coding.bo.common.compare;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * ValueCompare
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class ValueCompare implements ConditionCompare {

    private static final Set<String> TRUE_SETS = new HashSet<>();
    private static final Set<String> FALSE_SETS = new HashSet<>();

    static {
        // default true value
        TRUE_SETS.add("true");
        TRUE_SETS.add("TRUE");
        TRUE_SETS.add("是");
        TRUE_SETS.add("有");
        TRUE_SETS.add("1");
        TRUE_SETS.add("正常");
        TRUE_SETS.add("成功");
        TRUE_SETS.add("YES");
        TRUE_SETS.add("yes");
        // default false value
        FALSE_SETS.add("false");
        FALSE_SETS.add("FALSE");
        FALSE_SETS.add("否");
        FALSE_SETS.add("无");
        FALSE_SETS.add("0");
        FALSE_SETS.add("异常");
        FALSE_SETS.add("失败");
        FALSE_SETS.add("NO");
        FALSE_SETS.add("no");
    }

    @Override
    public int type() {
        return ConditionType.VALUE.getType();
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public CompareResult compare(Object input, Object benchmark, Function function) {
        if (benchmark == null) {
            return CompareResult.PASS;
        }
        if (input == null) {
            return CompareResult.UNKNOWN;
        }
        if (function != null) {
            return benchmark.equals(function.apply(input)) ? CompareResult.PASS : CompareResult.FAIL;
        }
        if (input instanceof Boolean) {
            return boolValueCompare((Boolean) input, benchmark);
        }
        if (benchmark instanceof Boolean) {
            return boolValueCompare((Boolean) benchmark, input);
        }
        return benchmark.equals(input) ? CompareResult.PASS : CompareResult.FAIL;
    }

    protected CompareResult boolValueCompare(Boolean input, Object benchmark) {
        if (TRUE_SETS.contains(String.valueOf(benchmark))) {
            return input ? CompareResult.PASS : CompareResult.FAIL;
        }
        if (FALSE_SETS.contains(String.valueOf(benchmark))) {
            return input ? CompareResult.FAIL : CompareResult.PASS;
        }
        return CompareResult.UNKNOWN;
    }

}
