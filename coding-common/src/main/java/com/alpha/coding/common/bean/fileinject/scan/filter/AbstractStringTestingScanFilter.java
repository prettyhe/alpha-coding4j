package com.alpha.coding.common.bean.fileinject.scan.filter;

import java.util.Objects;
import java.util.function.Function;

/**
 * AbstractStringTestingScanFilter
 *
 * @version 1.0
 * Date: 2020-03-18
 */
public abstract class AbstractStringTestingScanFilter implements ScanFilter {

    private Function<Object, String> targetStringFunc = String::valueOf;
    private Function<Object, String> benchmarkStringFunc = String::valueOf;

    public AbstractStringTestingScanFilter(Function<Object, String> targetStringFunc,
                                           Function<Object, String> benchmarkStringFunc) {
        this.targetStringFunc = targetStringFunc;
        this.benchmarkStringFunc = benchmarkStringFunc;
    }

    @Override
    public boolean match(Object target, Object benchmark) {
        if (Objects.equals(target, benchmark)) {
            return true;
        } else if (target == null || benchmark == null) {
            return false;
        }
        return doMatch(targetStringFunc.apply(target), benchmarkStringFunc.apply(benchmark));
    }

    protected abstract boolean doMatch(String target, String benchmark);
}
