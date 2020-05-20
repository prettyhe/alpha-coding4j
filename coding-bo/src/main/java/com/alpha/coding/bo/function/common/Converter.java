package com.alpha.coding.bo.function.common;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * Converter
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public interface Converter {

    /**
     * 获取Long
     */
    Function<Number, Long> toLong = number -> number == null ? null : number.longValue();
    /**
     * 获取int
     */
    Function<Number, Integer> toInt = number -> number == null ? null : number.intValue();
    /**
     * 获取int，如果为null则使用默认值
     */
    BiFunction<Number, Integer, Integer> toIntWithDefault = (num, def) -> num == null ? def : num.intValue();
    /**
     * 转成String，如果为null则使用默认值
     */
    BiFunction<Object, String, String> toStringWithDefault = (obj, def) -> obj == null ? def : String.valueOf(obj);
    /**
     * 转成BigDecimal，如果为null则使用默认值
     */
    BiFunction<String, BigDecimal, BigDecimal> toBigDecimalWithDefault = (str, def) ->
            str == null ? def : new BigDecimal(str.trim());

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
                return JSON.parseObject((String) s, t);
            } catch (Exception e) {
                return JSON.parseObject(JSON.toJSONString(s), t);
            }
        }
        return JSON.parseObject(JSON.toJSONString(s), t);
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
            return new JSONObject((Map) val);
        }
        return (JSONObject) JSON.toJSON(val);
    };

}
