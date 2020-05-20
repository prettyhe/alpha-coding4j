package com.alpha.coding.common.utils;

import java.lang.reflect.Type;

import com.alibaba.fastjson.JSON;

/**
 * ConvertUtils
 *
 * @version 1.0
 * Date: 2019-12-26
 */
public class ConvertUtils {

    /**
     * 利用json转换
     *
     * @param source     源
     * @param targetType 目标类型
     */
    public static <T> T jsonConvert(Object source, Type targetType) {
        if (source == null) {
            return null;
        }
        if (source instanceof String) {
            try {
                return JSON.parseObject((String) source, targetType);
            } catch (Exception e) {
                return JSON.parseObject(JSON.toJSONString(source), targetType);
            }
        }
        return JSON.parseObject(JSON.toJSONString(source), targetType);
    }

}
