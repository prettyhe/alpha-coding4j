/**
 * Copyright
 */
package com.alpha.coding.common.utils;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * RegexpUtils
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class RegexpUtils {

    /**
     * 生成大于等于指定非负整数的正则表达式
     */
    public static String genRegexpLargerEqual(int input) {
        return "(" + input + ")|" + merge(genSegs(input));
    }

    /**
     * 生成大于等于指定非负整数的正则表达式
     */
    public static List<String> genRegexpsLargerEqual(int input) {
        List<String> ret = Lists.newArrayList(String.valueOf(input));
        ret.addAll(genRegexpsLargerThan(input));
        return ret;
    }

    /**
     * 生成大于指定非负整数的正则表达式
     */
    public static String genRegexpLargerThan(int input) {
        return merge(genSegs(input));
    }

    /**
     * 生成大于指定非负整数的正则表达式
     */
    public static List<String> genRegexpsLargerThan(int input) {
        return genSegs(input);
    }

    private static List<Integer> getBitNumberValue(int number) {
        char[] chars = String.valueOf(number).toCharArray();
        List<Integer> ints = Lists.newArrayList();
        for (char ch : chars) {
            ints.add(Integer.valueOf(String.valueOf(ch)));
        }
        return ints;
    }

    private static String merge(List<String> segs) {
        StringBuilder sb = new StringBuilder();
        for (String seg : segs) {
            sb.append("(").append(seg).append(")|");
        }
        sb.deleteCharAt(sb.lastIndexOf("|"));
        return sb.toString();
    }

    private static List<String> genSegs(int number) {
        List<String> ret = Lists.newArrayList();
        List<Integer> ints = getBitNumberValue(number);
        for (int i = 0; i < ints.size(); i++) {
            String str = genReg(ints, i + 1);
            if (str == null) {
                continue;
            }
            ret.add(str);
        }
        ret.add(genReg(ints));
        return ret;
    }

    /**
     * @param index 从右往左的位数，起始位1
     */
    private static String genReg(List<Integer> ints, int index) {
        int start = ints.get(ints.size() - index) + 1;
        if (start == 10) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        if (index < ints.size()) {
            sb.append("[");
            for (int i = 0; i < ints.size() - index; i++) {
                sb.append(ints.get(i));
            }
            sb.append("]");
        }
        sb.append("[").append(start).append("-").append(9).append("]");
        for (int i = 0; i < index - 1; i++) {
            sb.append("[0-9]");
        }
        return sb.toString();
    }

    private static String genReg(List<Integer> ints) {
        return String.format("[1-9][0-9]{%d,}", ints.size());
    }
}
