package com.alpha.coding.common.utils.json;

import java.lang.reflect.Field;

import org.apache.commons.beanutils.ConvertUtils;

import com.alibaba.fastjson.JSONObject;
import com.alpha.coding.common.utils.FieldUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * JSONObjectParser
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class JSONObjectParser {

    public static <T> T parse(JSONObject jsonObject, Class<T> clz)
            throws IllegalAccessException, InstantiationException {
        return mapper(jsonObject, clz, null);
    }

    public static <T> T mapper(JSONObject jsonObject, Class<T> clz, T t)
            throws IllegalAccessException, InstantiationException {
        if (jsonObject == null) {
            return t;
        }
        T instance = t == null ? clz.newInstance() : t;
        Field[] declaredFields = clz.getDeclaredFields();
        return mapper(jsonObject, declaredFields, instance);
    }

    private static <T> T mapper(JSONObject jsonObject, Field[] declaredFields, T instance) {
        for (Field field : declaredFields) {
            field.setAccessible(true);
            JSONObjectPath annotation = field.getAnnotation(JSONObjectPath.class);
            if (annotation == null) {
                continue;
            }
            Object value = convert(JSONObjectUtils.getValue(jsonObject, annotation.path(), annotation.sep()),
                    annotation.javaType());
            FieldUtils.setField(instance, field.getName(), value);
        }
        return instance;
    }

    private static <T> T convert(Object value, Class<T> clz) {
        if (value == null) {
            return null;
        }
        try {
            Object convert = ConvertUtils.convert(value, clz);
            return (T) convert;
        } catch (Exception e) {
            log.error("convert-fail: value={}, clz={}", value, clz.getName(), e);
            throw e;
        }
    }

}
