package com.alpha.coding.common.utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;

/**
 * RegexpUtils
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class RegexpUtils {

    /**
     * 正则表达式匹配中文汉字
     */
    public final static String RE_CHINESE = "[\u4E00-\u9FFF]";
    /**
     * 正则表达式匹配中文字符串
     */
    public final static String RE_CHINESES = RE_CHINESE + "+";
    /**
     * 英文字母 、数字和下划线
     */
    public final static Pattern GENERAL = Pattern.compile("^\\w+$");
    /**
     * 数字
     */
    public final static Pattern NUMBERS = Pattern.compile("\\d+");
    /**
     * 字母
     */
    public final static Pattern WORD = Pattern.compile("[a-zA-Z]+");
    /**
     * 单个中文汉字
     */
    public final static Pattern CHINESE = Pattern.compile(RE_CHINESE);
    /**
     * 中文汉字
     */
    public final static Pattern CHINESES = Pattern.compile(RE_CHINESES);
    /**
     * 分组
     */
    public final static Pattern GROUP_VAR = Pattern.compile("\\$(\\d+)");
    /**
     * IP v4
     */
    public final static Pattern IPV4 = Pattern.compile("\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b");
    /**
     * IP v6
     */
    public final static Pattern IPV6 = Pattern.compile("(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]+|::(ffff(:0{1,4})?:)?((25[0-5]|(2[0-4]|1?[0-9])?[0-9])\\.){3}(25[0-5]|(2[0-4]|1?[0-9])?[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1?[0-9])?[0-9])\\.){3}(25[0-5]|(2[0-4]|1?[0-9])?[0-9]))");
    /**
     * 货币
     */
    public final static Pattern MONEY = Pattern.compile("^(\\d+(?:\\.\\d+)?)$");
    /**
     * 邮件，符合RFC 5322规范，正则来自：http://emailregex.com/
     */
    // public final static Pattern EMAIL = Pattern.compile("(\\w|.)+@\\w+(\\.\\w+){1,2}");
    public final static Pattern EMAIL = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])", Pattern.CASE_INSENSITIVE);
    /**
     * 移动电话
     */
    public final static Pattern MOBILE = Pattern.compile("(?:0|86|\\+86)?1[3456789]\\d{9}");
    /**
     * 18位身份证号码
     */
    public final static Pattern CITIZEN_ID = Pattern.compile("[1-9]\\d{5}[1-2]\\d{3}((0\\d)|(1[0-2]))(([012]\\d)|3[0-1])\\d{3}(\\d|X|x)");
    /**
     * 邮编
     */
    public final static Pattern ZIP_CODE = Pattern.compile("[1-9]\\d{5}(?!\\d)");
    /**
     * 生日
     */
    public final static Pattern BIRTHDAY = Pattern.compile("^(\\d{2,4})([/\\-.年]?)(\\d{1,2})([/\\-.月]?)(\\d{1,2})日?$");
    /**
     * URL
     */
    public final static Pattern URL = Pattern.compile("[a-zA-z]+://[^\\s]*");
    /**
     * Http URL
     */
    public final static Pattern URL_HTTP = Pattern.compile("(https://|http://)?([\\w-]+\\.)+[\\w-]+(:\\d+)*(/[\\w- ./?%&=]*)?");
    /**
     * 中文字、英文字母、数字和下划线
     */
    public final static Pattern GENERAL_WITH_CHINESE = Pattern.compile("^[\u4E00-\u9FFF\\w]+$");
    /**
     * UUID
     */
    public final static Pattern UUID = Pattern.compile("^[0-9a-z]{8}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{12}$");
    /**
     * 不带横线的UUID
     */
    public final static Pattern UUID_SIMPLE = Pattern.compile("^[0-9a-z]{32}$");
    /**
     * MAC地址正则
     */
    public static final Pattern MAC_ADDRESS = Pattern.compile("((?:[A-F0-9]{1,2}[:-]){5}[A-F0-9]{1,2})|(?:0x)(\\d{12})(?:.+ETHER)", Pattern.CASE_INSENSITIVE);
    /**
     * 16进制字符串
     */
    public static final Pattern HEX = Pattern.compile("^[a-f0-9]+$", Pattern.CASE_INSENSITIVE);
    /**
     * 时间正则
     */
    public static final Pattern TIME = Pattern.compile("\\d{1,2}:\\d{1,2}(:\\d{1,2})?");
    /**
     * 中国车牌号码（兼容新能源车牌）
     */
    public final static Pattern PLATE_NUMBER = Pattern.compile(
            //https://gitee.com/loolly/hutool/issues/I1B77H?from=project-issue
            "^(([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z](([0-9]{5}[ABCDEFGHJK])|([ABCDEFGHJK]([A-HJ-NP-Z0-9])[0-9]{4})))|" +
                    //https://gitee.com/loolly/hutool/issues/I1BJHE?from=project-issue
                    "([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领]\\d{3}\\d{1,3}[领])|" +
                    "([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-HJ-NP-Z0-9]{4}[A-HJ-NP-Z0-9挂学警港澳使领]))$");


    /**
     * 社会统一信用代码
     * <pre>
     * 第一部分：登记管理部门代码1位 (数字或大写英文字母)
     * 第二部分：机构类别代码1位 (数字或大写英文字母)
     * 第三部分：登记管理机关行政区划码6位 (数字)
     * 第四部分：主体标识码（组织机构代码）9位 (数字或大写英文字母)
     * 第五部分：校验码1位 (数字或大写英文字母)
     * </pre>
     */
    public static final Pattern CREDIT_CODE = Pattern.compile("^[0-9A-HJ-NPQRTUWXY]{2}\\d{6}[0-9A-HJ-NPQRTUWXY]{10}$");

    /**
     * 特殊字符
     */
    public static final Pattern SPECIAL_SYMBOL = Pattern.compile("[？~`!@#$%^&*()=_+\\\\{}|;:,.<>?～•！￥…×（）—『』【】、；'：《》，。\"\\[\\]\\-]");

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

    public static String filterSpecialSymbol(String text) {
        if (text == null) {
            return null;
        }
        Matcher m = SPECIAL_SYMBOL.matcher(text);
        return m.replaceAll("").trim();
    }

}
