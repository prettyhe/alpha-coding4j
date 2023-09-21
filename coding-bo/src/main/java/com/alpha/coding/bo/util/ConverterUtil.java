package com.alpha.coding.bo.util;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.util.ParameterizedTypeImpl;
import com.alpha.coding.bo.function.common.Converter;

/**
 * ConverterUtil
 *
 * @version 1.0
 * Date: 2022/3/11
 */
public class ConverterUtil {

    /**
     * 通过JSON序列化与反序列化转换
     *
     * @param src  源对象
     * @param type 目标类型
     */
    @SuppressWarnings({"unchecked"})
    public static <T> T convertByJson(Object src, Class<T> type) {
        return (T) Converter.jsonConvert.apply(src, type);
    }

    /**
     * 通过JSON序列化与反序列化转换，仅当类型不可转换时才转，否则直接使用类型转换
     *
     * @param src  源对象
     * @param type 目标类型
     */
    @SuppressWarnings({"unchecked"})
    public static <T> T convertByJsonIfNotAssignable(Object src, Class<T> type) {
        if (src == null) {
            return null;
        } else if (type.isAssignableFrom(src.getClass())) {
            return type.cast(src);
        } else {
            return (T) Converter.jsonConvert.apply(src, type);
        }
    }

    /**
     * 通过JSON序列化与反序列化转换为List
     *
     * @param src  源对象
     * @param type 目标类型
     */
    @SuppressWarnings({"unchecked"})
    public static <T> List<T> convertToListByJson(Object src, Class<T> type) {
        return (List<T>) Converter.jsonConvert.apply(src,
                TypeReference.intern(new ParameterizedTypeImpl(new Type[] {type}, ConverterUtil.class, List.class)));
    }

    /**
     * 通过JSON序列化与反序列化转换为Map
     *
     * @param src       源对象
     * @param keyType   key类型
     * @param valueType value类型
     */
    @SuppressWarnings({"unchecked"})
    public static <K, V> Map<K, V> convertToMapByJson(Object src, Class<K> keyType, Class<V> valueType) {
        return (Map<K, V>) Converter.jsonConvert.apply(src,
                TypeReference.intern(new ParameterizedTypeImpl(new Type[] {keyType, valueType},
                        ConverterUtil.class, Map.class)));
    }

}
