package com.alpha.coding.common.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            Pattern.compile("([^0-9]|\\b)(1[3456789]\\d)\\d{4}(\\d{4})([^0-9]|\\b)");

    /**
     * 从文本中提取身份证号码并进行脱敏处理。中间8位出生日期掩码
     *
     * @param text 待处理的文本
     * @return 脱敏后的文本
     */
    public static String desensitizeIDNumbers(String text) {
        Matcher matcher = ID_NUMBER_PATTERN.matcher(text);
        Map<int[], String> replaceMap = new LinkedHashMap<>();
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
                String maskedID = matcher.group(1) + addressPart + "********"
                        + sequencePart + checkDigit + matcher.group(6);
                replaceMap.put(new int[] {matcher.start(), matcher.end()}, maskedID);
            }
            if ("_".equals(matcher.group(6))) {
                // 回退一步，避免占用的尾字符刚好是'_'
                int currentEnd = matcher.end();
                if (currentEnd > 0) {
                    matcher.region(currentEnd - 1, text.length());
                }
            }
        }
        if (replaceMap.isEmpty()) {
            return text;
        }
        StringBuffer sb = new StringBuffer(text);
        // 执行替换
        replaceMap.forEach((k, v) -> sb.replace(k[0], k[1], v));
        return sb.toString();
    }

    /**
     * 从文本中提取手机号码并进行脱敏处理。中间4位掩码
     *
     * @param text 待处理的文本
     * @return 脱敏后的文本
     */
    public static String desensitizePhoneNumbers(String text) {
        Matcher matcher = PHONE_PATTERN.matcher(text);
        Map<int[], String> replaceMap = new LinkedHashMap<>();
        while (matcher.find()) {
            // 提取手机号码的部分
            String prefix = matcher.group(2);
            String suffix = matcher.group(3);
            // 脱敏处理
            String maskedPhone = matcher.group(1) + prefix + "****" + suffix + matcher.group(4);
            replaceMap.put(new int[] {matcher.start(), matcher.end()}, maskedPhone);
            if ("_".equals(matcher.group(4))) {
                // 回退一步，避免占用的尾字符刚好是'_'
                int currentEnd = matcher.end();
                if (currentEnd > 0) {
                    matcher.region(currentEnd - 1, text.length());
                }
            }
        }
        if (replaceMap.isEmpty()) {
            return text;
        }
        StringBuffer sb = new StringBuffer(text);
        // 执行替换
        replaceMap.forEach((k, v) -> sb.replace(k[0], k[1], v));
        return sb.toString();
    }

}
