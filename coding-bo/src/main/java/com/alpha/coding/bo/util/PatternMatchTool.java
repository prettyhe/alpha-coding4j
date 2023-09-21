package com.alpha.coding.bo.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PatternMatchTool
 *
 * @version 1.0
 * Date: 2022/4/27
 */
public class PatternMatchTool {

    private static final Map<String, Pattern> PATTERN_MAP = new ConcurrentHashMap<>();

    /**
     * 正则匹配
     *
     * @param pattern 正则表达式
     * @param target  目标
     * @see Matcher#matches()
     */
    public static boolean match(String pattern, String target) {
        return PATTERN_MAP.computeIfAbsent(pattern, Pattern::compile).matcher(target).matches();
    }

    /**
     * 获取缓存的正则模式
     *
     * @param pattern 正则表达式
     */
    public static Pattern pattern(String pattern) {
        return PATTERN_MAP.computeIfAbsent(pattern, Pattern::compile);
    }

}
