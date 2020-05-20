package com.alpha.coding.bo.common.compare;

import java.util.HashMap;
import java.util.Map;

/**
 * ConditionCompareFactory
 *
 * @version 1.0
 * Date: 2020/4/21
 */
public class ConditionCompareFactory {

    private static final Map<ConditionType, ConditionCompare> CACHE = new HashMap<>();

    public static ConditionCompare getConditionCompare(ConditionType type) {
        return CACHE.computeIfAbsent(type, t -> {
            switch (t) {
                case VALUE:
                    return new ValueCompare();
                case NON_VALUE:
                    return new NonValueCompare();
                case EXIST:
                    return new ExistCompare();
                case RANGE:
                    return new RangeCompare();
                case LEFT_OPEN_RANGE:
                    return new LeftOpenRangeCompare();
                case RIGHT_OPEN_RANGE:
                    return new RightOpenRangeCompare();
                case BOTH_OPEN_RANGE:
                    return new BothOpenRangeCompare();
                case INCLUDE:
                    return new IncludeCompare();
                case EXCLUDE:
                    return new ExcludeCompare();
                case REGEXP:
                    return new RegexpCompare();
                case CUSTOM:
                    return new CustomCompare();
                default:
                    throw new IllegalArgumentException("unknown ConditionType " + t);
            }
        });
    }

}
