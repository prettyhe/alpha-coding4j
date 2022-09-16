package com.alpha.coding.common.utils.json;

import java.lang.reflect.Field;

import com.alibaba.fastjson.JSONObject;
import com.alpha.coding.bo.annotation.JsonPath;
import com.alpha.coding.bo.function.common.Converter;
import com.alpha.coding.common.utils.FieldUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * JSONObjectParser
 *
 * @version 1.0
 */
@Slf4j
public class JSONObjectParser {

    public static <T> T parse(JSONObject jsonObject, Class<T> clz) {
        return mapper(jsonObject, clz, null);
    }

    public static <T> T mapper(JSONObject jsonObject, Class<T> clz)
            throws IllegalAccessException, InstantiationException {
        if (jsonObject == null) {
            return null;
        }
        return mapper(jsonObject, FieldUtils.findMatchedFields(clz, null).toArray(new Field[0]), clz.newInstance());
    }

    public static <T> T mapper(JSONObject jsonObject, Class<T> clz, T t) {
        if (jsonObject == null) {
            return t;
        }
        return mapper(jsonObject, FieldUtils.findMatchedFields(clz, null).toArray(new Field[0]), t);
    }

    private static <T> T mapper(JSONObject jsonObject, Field[] fields, T instance) {
        for (Field field : fields) {
            field.setAccessible(true);
            JSONObjectPath jsonObjectPath = field.getAnnotation(JSONObjectPath.class);
            if (jsonObjectPath != null) {
                FieldUtils.setField(instance, field.getName(),
                        Converter.jsonConvert.apply(JSONObjectUtils.getValue(jsonObject, jsonObjectPath.path(),
                                jsonObjectPath.sep()), field.getGenericType()));
                continue;
            }
            JsonPath jsonPath = field.getAnnotation(JsonPath.class);
            if (jsonPath != null) {
                FieldUtils.setField(instance, field.getName(),
                        Converter.jsonConvert.apply(JSONObjectUtils.getValue(jsonObject, jsonPath.path(),
                                "\\."), field.getGenericType()));
            }
        }
        return instance;
    }

}
