package com.alpha.coding.bo.util;

import com.alpha.coding.bo.function.common.Converter;

/**
 * ConverterUtil
 *
 * @version 1.0
 * Date: 2022/3/11
 */
public class ConverterUtil {

    public static <T> T convertByJson(Object src, Class<T> type) {
        return (T) Converter.jsonConvert.apply(src, type);
    }
}
