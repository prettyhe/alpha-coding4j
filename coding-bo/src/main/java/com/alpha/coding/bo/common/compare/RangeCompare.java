package com.alpha.coding.bo.common.compare;

import java.util.function.Function;

import com.alpha.coding.bo.base.ComparableRange;

/**
 * RangeCompare
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class RangeCompare implements ConditionCompare {

    @Override
    public int type() {
        return ConditionType.RANGE.getType();
    }

    @Override
    public CompareResult compare(Object input, Object benchmark, Function function) {
        if (benchmark == null) {
            return CompareResult.PASS;
        }
        if (!(benchmark instanceof ComparableRange)) {
            throw new IllegalArgumentException("benchmark not ComparableRange");
        }
        ComparableRange benchmarkRange = (ComparableRange) benchmark;
        if (benchmarkRange.getMin() == null && benchmarkRange.getMax() == null) {
            return CompareResult.PASS;
        }
        if (input == null) {
            return CompareResult.UNKNOWN;
        }
        // range类型的比较
        CompareResult rangeRet = CompareResult.PASS;
        if (input instanceof ComparableRange) {
            ComparableRange inputRange = (ComparableRange) input;
            if (benchmarkRange.getMin() != null) {
                if (inputRange.getMin() == null) {
                    rangeRet = CompareResult.and(rangeRet, CompareResult.UNKNOWN);
                } else {
                    final int compare =
                            (function == null ? inputRange.getMin() : (Comparable) function.apply(inputRange.getMin()))
                                    .compareTo(benchmarkRange.getMin());
                    rangeRet = CompareResult.and(rangeRet,
                            leftCompare(compare) ? CompareResult.PASS : CompareResult.FAIL);
                }
            }
            if (benchmarkRange.getMax() != null) {
                if (inputRange.getMax() == null) {
                    rangeRet = CompareResult.and(rangeRet, CompareResult.UNKNOWN);
                } else {
                    final int compare =
                            (function == null ? inputRange.getMax() : (Comparable) function.apply(inputRange.getMax()))
                                    .compareTo(benchmarkRange.getMax());
                    rangeRet = CompareResult.and(rangeRet,
                            rightCompare(compare) ? CompareResult.PASS : CompareResult.FAIL);
                }
            }
        } else if (function != null) {
            rangeRet = CompareResult.and(rangeRet,
                    compareValueToRange((Comparable) function.apply(input), benchmarkRange));
        } else if (input instanceof Comparable) {
            rangeRet = CompareResult.and(rangeRet,
                    compareValueToRange((Comparable) input, benchmarkRange));
        } else {
            throw new IllegalArgumentException("input cannot apply to Comparable");
        }
        return rangeRet;
    }

    private CompareResult compareValueToRange(Comparable value, ComparableRange benchmarkRange) {
        if (value == null) {
            return CompareResult.UNKNOWN;
        }
        CompareResult result = CompareResult.PASS;
        if (benchmarkRange.getMin() != null) {
            result = CompareResult.and(result,
                    leftCompare(value.compareTo(benchmarkRange.getMin())) ? CompareResult.PASS : CompareResult.FAIL);
        }
        if (benchmarkRange.getMax() != null) {
            result = CompareResult.and(result,
                    rightCompare(value.compareTo(benchmarkRange.getMax())) ? CompareResult.PASS : CompareResult.FAIL);
        }
        return result;
    }

    /**
     * val compare to left
     */
    protected boolean leftCompare(int compareToLeft) {
        return compareToLeft >= 0;
    }

    /**
     * val compare to right
     */
    protected boolean rightCompare(int compareToRight) {
        return compareToRight <= 0;
    }

}
