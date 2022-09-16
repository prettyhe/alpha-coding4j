package com.alpha.coding.bo.util;

import java.lang.reflect.Type;
import java.util.List;

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

    @SuppressWarnings({"unchecked"})
    public static <T> T convertByJson(Object src, Class<T> type) {
        return (T) Converter.jsonConvert.apply(src, type);
    }

    @SuppressWarnings({"unchecked"})
    public static <T> List<T> convertToListByJson(Object src, Class<T> type) {
        return (List<T>) Converter.jsonConvert.apply(src,
                TypeReference.intern(new ParameterizedTypeImpl(new Type[] {type}, ConverterUtil.class, List.class)));
    }
}
