package com.alpha.coding.bo.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * PatternMatchTool
 *
 * @version 1.0
 * Date: 2022/4/27
 */
public class PatternMatchTool {

    private static final Map<String, Pattern> PATTERN_MAP = new ConcurrentHashMap<>();

    public static boolean match(String pattern, String target) {
        return PATTERN_MAP.computeIfAbsent(pattern, Pattern::compile).matcher(target).matches();
    }

    public static Pattern pattern(String pattern) {
        return PATTERN_MAP.computeIfAbsent(pattern, Pattern::compile);
    }

}
