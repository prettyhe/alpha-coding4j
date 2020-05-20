package com.alpha.coding.common.utils;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

/**
 * SqlUtils
 *
 * @author nick
 * @version 1.0
 * Date: 2020-02-21
 */
public class SqlUtils {

    public static String append(String s) {
        String ret = s.replaceAll("\\\\", "\\\\\\\\");
        ret = ret.replaceAll("'", "\\\\'");
        return "'" + ret + "'";
    }

    public static String append(Date d) {
        String dateStr = DateUtils.format(d, "yyyy-MM-dd");
        return "'" + dateStr + "'";
    }

    /**
     * change ' to \'
     *
     * @param s 输入
     *
     * @return result
     */
    public static String escapeSql(String s) {
        if (s == null) {
            return null;
        }
        String ret = s.replaceAll("\\\\", "\\\\\\\\");
        ret = ret.replaceAll("'", "\\\\'");
        return ret;
    }

    /**
     * change % to \% change ' to \'
     *
     * @param s 输入
     *
     * @return result
     */
    public static String escapeSqlLike(String s) {
        if (s == null) {
            return null;
        }
        String ret = s.replaceAll("\\\\", "\\\\\\\\");
        ret = ret.replaceAll("%", "\\\\%");
        ret = ret.replaceAll("'", "\\\\'");
        return ret;
    }

    public static String sqlLike(String str) {
        if (str == null) {
            return null;
        }
        return "%" + str + "%";
    }

    /**
     * 条件拼成sql
     *
     * @param condition 条件
     *
     * @return result
     */
    public static String toConditionSql(List<String> condition) {
        if (condition == null || condition.size() <= 0) {
            return " ";
        }
        StringBuffer sb = new StringBuffer();
        sb.append(condition.get(0));
        for (int i = 1; i < condition.size(); i++) {
            sb.append(" and " + condition.get(i));
        }
        return sb.toString();
    }

    public static String toMd5List(Collection<String> urls) {
        if (CollectionUtils.isEmpty(urls)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String url : urls) {
            sb.append("'").append(MD5Utils.md5(url)).append("',");
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        return sb.toString();
    }
}
