package com.alpha.coding.bo.function.common;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

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
            (t == null && u == null) || (t != null && u != null && Integer.parseInt(t.trim()) == u.intValue());

    /**
     * 判断一个object的整数部分是否等于预期整数值
     */
    BiPredicate<Object, Integer> testIntValue = (t, u) -> {
        if (t == null && u == null) {
            return true;
        }
        if ((t == null && u != null) || (t != null && u == null)) {
            return false;
        }
        if (t instanceof Number) {
            return ((Number) t).intValue() == u.intValue();
        }
        try {
            final BigDecimal bigDecimal = new BigDecimal(String.valueOf(t).trim());
            return bigDecimal.intValue() == u.intValue();
        } catch (Exception e) {
            return false;
        }
    };

    /**
     * 判断集合中是否存在元素
     */
    BiPredicate<Number, Set<Integer>> testContainsInt = (Number t, Set<Integer> u) ->
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
            URL url = new URL(t);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    };

    /**
     * 判断包含
     */
    TiPredicate<Object, Set<Object>, Function<Object, Object>> testContains =
            (Object t, Set<Object> u, Function<Object, Object> v) ->
                    (t == null && u == null) || (t != null && u != null && u.contains(v.apply(t)));

}
