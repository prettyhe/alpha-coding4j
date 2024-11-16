package com.alpha.coding.bo.function.common;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;

/**
 * Converter
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public interface Converter {

    /**
     * 获取Integer
     */
    Function<Number, Integer> toInt = (Number number) ->
            number == null ? null : number.intValue();

    /**
     * 获取Long
     */
    Function<Number, Long> toLong = (Number number) ->
            number == null ? null : number.longValue();
    /**
     * 获取Byte
     */
    Function<Number, Byte> toByte = (Number number) ->
            number == null ? null : number.byteValue();
    /**
     * 获取Short
     */
    Function<Number, Short> toShort = (Number number) ->
            number == null ? null : number.shortValue();

    /**
     * 获取Float
     */
    Function<Number, Float> toFloat = (Number number) ->
            number == null ? null : number.floatValue();

    /**
     * 获取Double
     */
    Function<Number, Double> toDouble = (Number number) ->
            number == null ? null : number.doubleValue();

    /**
     * 获取int，如果为null则使用默认值
     */
    BiFunction<Number, Integer, Integer> toIntWithDefault = (Number number, Integer defaultVal) ->
            number == null ? defaultVal : number.intValue();

    /**
     * toString
     */
    Function<Object, String> TO_STRING = o -> o == null ? null : String.valueOf(o);

    /**
     * 转成String，如果为null则使用默认值
     */
    BiFunction<Object, String, String> toStringWithDefault = (Object o, String defaultVal) ->
            o == null ? defaultVal : String.valueOf(o);

    /**
     * 转成BigDecimal，如果为null则使用默认值
     */
    BiFunction<String, BigDecimal, BigDecimal> toBigDecimalWithDefault = (String t, BigDecimal u) ->
            t == null ? u : new BigDecimal(t.trim());

    /**
     * 利用json转换
     * <p>s=>源对象，t=>目标对象类型</p>
     */
    BiFunction<Object, Type, Object> jsonConvert = (s, t) -> {
        if (s == null) {
            return null;
        }
        if (s instanceof String) {
            try {
                return JSON.parseObject((String) s, t, Feature.OrderedField);
            } catch (Exception e) {
                return JSON.parseObject(JSON.toJSONString(s), t, Feature.OrderedField);
            }
        }
        return JSON.parseObject(JSON.toJSONString(s), t, Feature.OrderedField);
    };

    /**
     * Object => JSONObject
     */
    Function<Object, JSONObject> toJSONObject = val -> {
        if (val == null) {
            return null;
        }
        if (val instanceof JSONObject) {
            return (JSONObject) val;
        }
        if (val instanceof Map) {
            final JSONObject result = new JSONObject(true);
            ((Map) (val)).forEach((k, v) -> result.put(String.valueOf(k), v));
            return result;
        }
        if (val instanceof String) {
            return JSON.parseObject((String) val, Feature.OrderedField);
        }
        return (JSONObject) JSON.toJSON(val);
    };

    /**
     * Object => JSONObject, when result is null, return a new JSONObject
     */
    Function<Object, JSONObject> toJSONObjectNewWhenNull =
            toJSONObject.andThen(p -> p == null ? new JSONObject(true) : p);

    /**
     * 数值间相互转化
     */
    BiFunction<Number, Type, Number> convertNumber = (number, type) -> {
        if (number == null) {
            return null;
        }
        if (type == byte.class || type == Byte.class) {
            return number.byteValue();
        } else if (type == short.class || type == Short.class) {
            return number.shortValue();
        } else if (type == int.class || type == Integer.class) {
            return number.intValue();
        } else if (type == long.class || type == Long.class) {
            return number.longValue();
        } else if (type == float.class || type == Float.class) {
            return number.floatValue();
        } else if (type == double.class || type == Double.class) {
            return number.doubleValue();
        } else if (type == BigInteger.class) {
            return new BigInteger(String.valueOf(number));
        } else if (type == BigDecimal.class) {
            return number instanceof BigDecimal ? (BigDecimal) number : new BigDecimal(String.valueOf(number));
        } else if (type == AtomicInteger.class) {
            return new AtomicInteger(number.intValue());
        } else if (type == AtomicLong.class) {
            return new AtomicLong(number.longValue());
        } else {
            try {
                if (!Number.class.isAssignableFrom(Class.forName(type.getTypeName()))) {
                    throw new UnsupportedOperationException("not support for " + type.getTypeName());
                }
            } catch (ClassNotFoundException e) {
                throw new UnsupportedOperationException("not support for " + type.getTypeName());
            }
            return number;
        }
    };

    /**
     * 转化为数值
     */
    BiFunction<Object, Type, Number> convertToNumber = (val, type) -> {
        if (val == null) {
            return null;
        }
        if (val instanceof Number) {
            return convertNumber.apply((Number) val, type);
        }
        return convertNumber.apply(new BigDecimal(String.valueOf(val)), type);
    };

}
