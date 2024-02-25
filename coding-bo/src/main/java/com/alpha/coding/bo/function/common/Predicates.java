package com.alpha.coding.bo.function.common;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import com.alpha.coding.bo.function.TiPredicate;

/**
 * Predicates
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public interface Predicates {

    /**
     * 非空校验
     */
    Predicate<Object> notEmptyPredicate = t -> t != null && String.valueOf(t).length() > 0;

    /**
     * 判断一个字符串是否是预期的整数字符串
     */
    BiPredicate<String, Integer> testIntStr = (t, u) ->
            (t == null && u == null) || (t != null && u != null && Integer.parseInt(t.trim()) == u);

    /**
     * 判断一个object的整数部分是否等于预期整数值
     */
    BiPredicate<Object, Integer> testIntValue = (t, u) -> {
        if (t == null && u == null) {
            return true;
        }
        if (t == null || u == null) {
            return false;
        }
        if (t instanceof Number) {
            return ((Number) t).intValue() == u;
        }
        try {
            final BigDecimal bigDecimal = new BigDecimal(String.valueOf(t).trim());
            return bigDecimal.intValue() == u;
        } catch (Exception e) {
            return false;
        }
    };

    /**
     * 判断集合中是否存在元素
     */
    BiPredicate<Number, Set<Integer>> testContainsInt = (t, u) ->
            (t == null && u == null) || (t != null && u != null && u.contains(t.intValue()));

    /**
     * 是否判断: true/1/是 都被标记为true，空值以及其它值都被标记为否
     */
    Predicate<Object> boolPredicate = t -> {
        if (t == null) {
            return false;
        }
        if (t instanceof Boolean) {
            return (boolean) t;
        }
        String val = String.valueOf(t).trim();
        return "true".equalsIgnoreCase(val) || "1".equals(val) || "是".equals(val);
    };

    /**
     * 判断是否是url
     */
    Predicate<String> urlPredicate = t -> {
        try {
            new URL(t);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    };

    /**
     * 判断包含
     */
    TiPredicate<Object, Set<Object>, Function<Object, Object>> testContains = (t, u, v) ->
            (t == null && u == null) || (t != null && u != null && u.contains(v.apply(t)));

    /**
     * 判断两时间是否为同一天
     */
    BiPredicate<Date, Date> testSameDay = (d1, d2) -> {
        Calendar c1 = Calendar.getInstance();
        c1.setTime(d1);
        Calendar c2 = Calendar.getInstance();
        c2.setTime(d2);
        return c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH)
                && c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH)
                && c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR);
    };

    /**
     * 判断两时间是否为同一月
     */
    BiPredicate<Date, Date> testSameMonth = (d1, d2) -> {
        Calendar c1 = Calendar.getInstance();
        c1.setTime(d1);
        Calendar c2 = Calendar.getInstance();
        c2.setTime(d2);
        return c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH)
                && c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR);
    };

    /**
     * 判断两个数值是否相等
     */
    TiPredicate<Number, Number, Function<Number, Object>> testNumberEqual = (n1, n2, func) ->
            (n1 == null && n2 == null) || (n1 != null && n2 != null && Objects.equals(func.apply(n1), func.apply(n2)));

    /**
     * 判断两个数字整数部分是否相等
     */
    BiPredicate<Number, Number> testIntEqual = (n1, n2) ->
            (n1 == null && n2 == null) || (n1 != null && n2 != null && n1.intValue() == n2.intValue());

    /**
     * 判断两个数字整数部分是否相等
     */
    BiPredicate<Number, Number> testLongEqual = (n1, n2) ->
            (n1 == null && n2 == null) || (n1 != null && n2 != null && n1.longValue() == n2.longValue());

    /**
     * 判断数值是否被包含
     */
    TiPredicate<Number, Number[], Function<Number, Object>> testNumberInclude =
            (n, a, func) -> Arrays.stream(a).anyMatch(p -> testNumberEqual.test(n, p, func));

    /**
     * 判断整数部分是否被包含
     */
    BiPredicate<Number, Number[]> testIntInclude = (n, a) -> Arrays.stream(a).anyMatch(p -> testIntEqual.test(n, p));

    /**
     * 判断整数部分是否被包含
     */
    BiPredicate<Number, Number[]> testLongInclude = (n, a) -> Arrays.stream(a).anyMatch(p -> testLongEqual.test(n, p));

    /**
     * 判断字符串是否为空
     */
    Predicate<CharSequence> isEmptyStr = cs -> cs == null || cs.length() == 0;

    /**
     * 判断字符串是否非空
     */
    Predicate<CharSequence> isNotEmptyStr = isEmptyStr.negate();

    /**
     * 判断字符串是否为空白
     */
    Predicate<CharSequence> isBlankStr = cs -> isEmptyStr.test(cs)
            || IntStream.range(0, cs.length()).map(cs::charAt).allMatch(Character::isWhitespace);

    /**
     * 判断字符串是否为非空白
     */
    Predicate<CharSequence> isNotBlankStr = isBlankStr.negate();

    /**
     * 判断字符串是否为数字
     */
    Predicate<CharSequence> isNumericStr = cs -> isNotEmptyStr.test(cs)
            && IntStream.range(0, cs.length()).map(cs::charAt).allMatch(Character::isDigit);

    /**
     * 判断字符串是否为整数(Integer)字符串
     */
    Predicate<CharSequence> isIntegerStr = cs -> {
        if (isNotBlankStr.test(cs)) {
            try {
                Integer.parseInt(cs.toString());
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    };

    /**
     * 判断字符串是否为整数(Long)字符串
     */
    Predicate<CharSequence> isLongStr = cs -> {
        if (isNotBlankStr.test(cs)) {
            try {
                Long.parseLong(cs.toString());
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    };

    /**
     * 判断字符串是否为数值(BigDecimal)字符串
     */
    Predicate<CharSequence> isBigDecimalStr = cs -> {
        if (isNotBlankStr.test(cs)) {
            try {
                new BigDecimal(cs.toString());
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    };

    /**
     * 判断字符串是否为JSON对象字符串
     */
    Predicate<CharSequence> isJsonObjStr = cs -> {
        if (cs != null) {
            return cs.charAt(0) == '{' && cs.charAt(cs.length() - 1) == '}';
        }
        return false;
    };

    /**
     * 判断字符串是否为JSON数组字符串
     */
    Predicate<CharSequence> isJsonArrayStr = cs -> {
        if (cs != null) {
            return cs.charAt(0) == '[' && cs.charAt(cs.length() - 1) == ']';
        }
        return false;
    };

    /**
     * 时间是否在指定区间内(闭区间)
     */
    TiPredicate<Date, Date, Date> isBetween = (d, st, et) -> d.compareTo(st) >= 0 && d.compareTo(et) <= 0;

    /**
     * 时间是否在指定区间内(开区间)
     */
    TiPredicate<Date, Date, Date> isBetweenOpen = (d, st, et) -> d.compareTo(st) > 0 && d.compareTo(et) < 0;

    /**
     * 时间是否在指定区间内(前开后闭)
     */
    TiPredicate<Date, Date, Date> isBetweenOpenClosed = (d, st, et) -> d.compareTo(st) > 0 && d.compareTo(et) <= 0;

    /**
     * 时间是否在指定区间内(前闭后开)
     */
    TiPredicate<Date, Date, Date> isBetweenClosedOpen = (d, st, et) -> d.compareTo(st) >= 0 && d.compareTo(et) < 0;

    /**
     * 判断集合是否为空
     */
    Predicate<Collection<?>> isEmptyColl = coll -> coll == null || coll.isEmpty();

    /**
     * 判断集合是否非空
     */
    Predicate<Collection<?>> isNotEmptyColl = isEmptyColl.negate();

    /**
     * 判断Map是否为空
     */
    Predicate<Map<?, ?>> isEmptyMap = map -> map == null || map.isEmpty();

    /**
     * 判断Map是否非空
     */
    Predicate<Map<?, ?>> isNotEmptyMap = isEmptyMap.negate();

}
