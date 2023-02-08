package com.alpha.coding.common.bean.fileinject.scan.filter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * RegexPatternScanFilter
 *
 * @version 1.0
 * Date: 2020-03-18
 */
public class RegexPatternScanFilter extends AbstractStringTestingScanFilter {

    private static final ConcurrentMap<String, Pattern> patternMap = new ConcurrentHashMap<>();

    public RegexPatternScanFilter(Function<Object, String> targetStringFunc,
                                  Function<Object, String> benchmarkStringFunc) {
        super(targetStringFunc, benchmarkStringFunc);
    }

    @Override
    protected boolean doMatch(String target, String benchmark) {
        return patternMap.computeIfAbsent(benchmark, Pattern::compile).matcher(target).matches();
    }
}
