package com.alpha.coding.common.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * VersionUtils
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class VersionUtils {

    /**
     * 只包含数字的版本号编码成可比较的纯数值
     *
     * @param version     版本号，如9.5.11
     * @param sepCnt      版本组成部分数，不要太长,太长会溢出
     * @param sepMaxWidth 版本组成部分每部分最大宽度，不要太长，太长会溢出
     */
    public static long numberVersionToCode(String version, int sepCnt, int sepMaxWidth) {
        final String[] seps = version.trim().split("\\.");
        long step = 1;
        for (int i = 0; i < sepMaxWidth; i++) {
            step *= 10;
        }
        long ret = 0;
        for (int i = 0; i < sepCnt; i++) {
            if (seps.length <= i) {
                continue;
            }
            long tmpStep = 1;
            for (int j = 0; j < sepCnt - i - 1; j++) {
                tmpStep *= step;
            }
            String sep = seps[i];
            if (sep.length() > sepMaxWidth) {
                sep = sep.substring(0, sepMaxWidth);
            }
            ret += tmpStep * Long.parseLong(sep);
        }
        return ret;
    }

    /**
     * 版本code解码成只包含数字的版本号
     *
     * @param code        版本号，如10001000
     * @param sepCnt      版本组成部分数，为null则不限定
     * @param sepMaxWidth 版本组成部分每部分最大宽度
     */
    public static String codeToNumberVersion(long code, Integer sepCnt, int sepMaxWidth) {
        long step = 1;
        for (int i = 0; i < sepMaxWidth; i++) {
            step *= 10;
        }
        List<String> list = new ArrayList<>();
        long remain = code;
        while (remain > 0) {
            list.add(String.valueOf(remain % step));
            remain = remain / step;
        }
        Collections.reverse(list);
        if (sepCnt != null && list.size() > sepCnt) {
            list = list.subList(0, sepCnt);
        }
        return String.join(".", list);
    }

    /**
     * 版本比较(x.y.z)
     */
    public static int compare(String v1, String v2) {
        final String[] a1 = v1.split("\\.");
        final String[] a2 = v2.split("\\.");
        final int max = Math.max(a1.length, a2.length);
        Function<String[], String[]> function = t -> {
            if (t.length == max) {
                return t;
            } else {
                final String[] ret = new String[max];
                System.arraycopy(t, 0, ret, 0, t.length);
                for (int i = t.length; i < max; i++) {
                    ret[i] = "0";
                }
                return ret;
            }
        };
        final String[] c1 = function.apply(a1);
        final String[] c2 = function.apply(a2);
        for (int i = 0; i < max; i++) {
            final int compare = c1[i].compareTo(c2[i]);
            if (compare != 0) {
                return compare;
            }
        }
        return 0;
    }

}
