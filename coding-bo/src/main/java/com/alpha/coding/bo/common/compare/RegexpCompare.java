package com.alpha.coding.bo.common.compare;

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

    @Override
    public int type() {
        return ConditionType.EXCLUDE.getType();
    }

    @Override
    public CompareResult compare(Object input, Object benchmark, Function function) {
        if (benchmark == null) {
            return CompareResult.PASS;
        }
        if (input == null) {
            return CompareResult.UNKNOWN;
        }
        Pattern pattern = Pattern.compile(String.valueOf(benchmark));
        Matcher matcher = pattern.matcher(String.valueOf(function == null ? input : function.apply(input)));
        return matcher.find() ? CompareResult.PASS : CompareResult.FAIL;
    }

}
