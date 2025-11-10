package com.alpha.coding.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alpha.coding.bo.base.Triple;
import com.alpha.coding.bo.base.Tuple;
import com.alpha.coding.bo.validation.CommonTool;

/**
 * DesensitizeUtils
 *
 * @version 1.0
 * @date 2024年10月31日
 */
public class DesensitizeUtils {

    /**
     * 提取身份证号正则表达式
     */
    private static final Pattern ID_NUMBER_PATTERN =
            Pattern.compile("([^0-9]|\\b)(\\d{6})(\\d{8})(\\d{3})(\\d|X|x)([^0-9]|\\b)");
    /**
     * 提取手机号正则表达式
     */
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("([^0-9]|\\b)(1[3456789]\\d)(\\d{4})(\\d{4})([^0-9]|\\b)");

    /**
     * 从文本中提取身份证号码并进行替换处理
     *
     * @param text           待处理的文本
     * @param replaceHandler 身份证号替换处理回调(6+8+4模式)
     * @return 替换后的文本
     */
    public static String replaceIDNumbers(String text,
                                          Function<Triple<String, String, String>, String> replaceHandler) {
        final Matcher matcher = ID_NUMBER_PATTERN.matcher(text);
        final List<Tuple<int[], String>> replaces = new ArrayList<>();
        while (matcher.find()) {
            // 提取身份证号码的部分
            String addressPart = matcher.group(2);
            String birthPart = matcher.group(3);
            String sequencePart = matcher.group(4);
            String checkDigit = matcher.group(5);
            String idNumber = addressPart + birthPart + sequencePart + checkDigit;
            // 校验是否是合法身份证号
            final boolean validCard18 = CommonTool.isValidCard18(idNumber);
            if (validCard18) {
                // 脱敏处理
                String maskedID = matcher.group(1)
                        + replaceHandler.apply(Triple.of(addressPart, birthPart, sequencePart + checkDigit))
                        + matcher.group(6);
                replaces.add(Tuple.of(new int[] {matcher.start(), matcher.end()}, maskedID));
            }
            if ("_".equals(matcher.group(6))) {
                // 回退一步，避免占用的尾字符刚好是'_'
                int currentEnd = matcher.end();
                if (currentEnd > 0) {
                    matcher.region(currentEnd - 1, text.length());
                }
            }
        }
        if (replaces.isEmpty()) {
            return text;
        }
        // 执行替换
        return doReplace(text, replaces).toString();
    }

    /**
     * 从文本中提取身份证号码并进行脱敏处理。中间8位出生日期掩码
     *
     * @param text 待处理的文本
     * @return 脱敏后的文本
     */
    public static String desensitizeIDNumbers(String text) {
        return replaceIDNumbers(text, t -> t.getF() + "********" + t.getT());
    }

    /**
     * 按需要替换的位置顺序替换
     *
     * @param text     原始字符串
     * @param replaces 替换描述(F:int[]=>原始字符中待替换子串的起始结束位置, S:string=>替换后字符串)
     */
    public static StringBuffer doReplace(String text, List<Tuple<int[], String>> replaces) {
        final StringBuffer origin = new StringBuffer(text);
        if (replaces.stream().allMatch(t -> t.getS() != null && t.getS().length() == (t.getF()[1] - t.getF()[0]))) {
            replaces.forEach(t -> origin.replace(t.getF()[0], t.getF()[1], t.getS()));
            return origin;
        }
        final StringBuffer result = new StringBuffer(text.length());
        // 执行替换
        int start = 0;
        for (Tuple<int[], String> triple : replaces) {
            if (start <= triple.getF()[0]) {
                result.append(origin, start, triple.getF()[0]);
                result.append(triple.getS());
                start = triple.getF()[1];
            }
        }
        if (start < text.length()) {
            result.append(origin, start, text.length());
        }
        return result;
    }

    /**
     * 从文本中提取手机号码并进行替换处理
     *
     * @param text           待处理的文本
     * @param replaceHandler 手机号替换处理回调(匹配信息)
     * @return 替换后的文本
     */
    private static String replacePhoneNumbersWithMatcher(String text,
                                                         Function<Matcher, String> replaceHandler) {
        final Matcher matcher = PHONE_PATTERN.matcher(text);
        final List<Tuple<int[], String>> replaces = new ArrayList<>();
        while (matcher.find()) {
            // 替换处理
            String replacedPhone = matcher.group(1)
                    + replaceHandler.apply(matcher)
                    + matcher.group(5);
            replaces.add(Tuple.of(new int[] {matcher.start(), matcher.end()}, replacedPhone));
            if ("_".equals(matcher.group(5))) {
                // 回退一步，避免占用的尾字符刚好是'_'
                int currentEnd = matcher.end();
                if (currentEnd > 0) {
                    matcher.region(currentEnd - 1, text.length());
                }
            }
        }
        if (replaces.isEmpty()) {
            return text;
        }
        return doReplace(text, replaces).toString();
    }

    /**
     * 从文本中提取手机号码并进行替换处理
     *
     * @param text           待处理的文本
     * @param replaceHandler 手机号替换处理回调(3+4+4模式)
     * @return 替换后的文本
     */
    public static String replacePhoneNumbers(String text,
                                             Function<Triple<String, String, String>, String> replaceHandler) {
        return replacePhoneNumbersWithMatcher(text,
                matcher -> replaceHandler.apply(Triple.of(matcher.group(2), matcher.group(3), matcher.group(4))));
    }

    /**
     * 从文本中提取手机号码并进行替换处理
     *
     * @param text           待处理的文本
     * @param replaceHandler 手机号替换处理回调(手机号在原字符串起始位置、手机号在原字符串结束位置、匹配到的手机号)
     * @return 替换后的文本
     */
    public static String replacePhoneNumbersWithIndex(String text,
                                                      Function<Triple<Integer, Integer, String>, String> replaceHandler) {
        return replacePhoneNumbersWithMatcher(text,
                matcher -> replaceHandler.apply(Triple.of(matcher.start() + matcher.group(1).length(),
                        matcher.end() - matcher.group(5).length(),
                        matcher.group(2) + matcher.group(3) + matcher.group(4))));
    }

    /**
     * 从文本中提取手机号码并进行脱敏处理。中间4位掩码
     *
     * @param text 待处理的文本
     * @return 脱敏后的文本
     */
    public static String desensitizePhoneNumbers(String text) {
        return replacePhoneNumbers(text, t -> t.getF() + "****" + t.getT());
    }

}
