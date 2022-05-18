package com.alpha.coding.common.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * StringUtils
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class StringUtils {

    private static final String SEP_DOT = "\\.";
    private static final String DOT = ".";
    private static final String EMPTY_STRING = "";

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((!Character.isWhitespace(str.charAt(i)))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static boolean isNumeric(String str) {
        if (str == null) {
            return false;
        } else {
            int sz = str.length();

            for (int i = 0; i < sz; ++i) {
                if (!Character.isDigit(str.charAt(i))) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * 对字符串以点号切割并进行缩写
     * <li>
     * 保留0个: abc.efg.aaa => a.e.a
     * </li>
     * <li>
     * 保留1个: abc.efg.aaa => a.e.aaa
     * </li>
     *
     * @param text        字符串
     * @param reserveLast 最后保留个数
     */
    public static String abbreviateDotSplit(String text, int reserveLast) {
        if (text == null) {
            return null;
        }
        if (reserveLast < 0) {
            reserveLast = 0;
        }
        try {
            final String[] arr = text.split(SEP_DOT);
            if (reserveLast >= arr.length) {
                return text;
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < arr.length - reserveLast; i++) {
                sb.append(arr[i], 0, 1).append(DOT);
            }
            for (int i = arr.length - reserveLast; i < arr.length; i++) {
                sb.append(arr[i]).append(DOT);
            }
            sb.deleteCharAt(sb.lastIndexOf(DOT));
            return sb.toString();
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("abbreviate text fail, {}", e.getMessage());
            }
            return text;
        }
    }

    public static String join(String[] array, String split) {
        if (array == null || array.length == 0) {
            return EMPTY_STRING;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                sb.append(split);
            }
            sb.append(array[i]);
        }
        return sb.toString();
    }

}
