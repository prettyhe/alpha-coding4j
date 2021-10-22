package com.alpha.coding.bo.common.compare;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RegexpCompare
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class RegexpCompare implements ConditionCompare {

    private static final Map<String, Pattern> PATTERN_MAP = new ConcurrentHashMap<>();

    @Override
    public int type() {
        return ConditionType.REGEXP.getType();
    }

    @Override
    public CompareResult compare(Object input, Object benchmark, Function function) {
        if (benchmark == null) {
            return CompareResult.PASS;
        }
        if (input == null) {
            return CompareResult.UNKNOWN;
        }
        Pattern pattern = PATTERN_MAP.computeIfAbsent(String.valueOf(benchmark), Pattern::compile);
        Matcher matcher = pattern.matcher(String.valueOf(function == null ? input : function.apply(input)));
        return matcher.matches() ? CompareResult.PASS : CompareResult.FAIL;
    }

}
