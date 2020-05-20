package com.alpha.coding.common.bean.fileinject.scan.filter;

import java.io.File;
import java.util.function.Function;

import lombok.Getter;

/**
 * FileNameRegexPatternScanFilter
 *
 * @version 1.0
 * Date: 2020-03-18
 */
public class FileNameRegexPatternScanFilter extends RegexPatternScanFilter {

    @Getter
    private final String pattern;

    public FileNameRegexPatternScanFilter(Function<Object, String> targetStringFunc,
                                          Function<Object, String> benchmarkStringFunc,
                                          String pattern) {
        super(targetStringFunc, benchmarkStringFunc);
        this.pattern = pattern;
    }

    public FileNameRegexPatternScanFilter(String pattern) {
        super(f -> ((File) f).getName(), String::valueOf);
        this.pattern = pattern;
    }

}
