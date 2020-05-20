package com.alpha.coding.common.utils.json;

import java.lang.reflect.Field;

import com.alibaba.fastjson.JSONObject;

/**
 * JSONObjectGenerator
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class JSONObjectGenerator {

    public static JSONObject generate(Object object) throws IllegalAccessException {
        if (object == null) {
            return null;
        }
        JSONObject jsonObject = new JSONObject();
        Class<?> clz = object.getClass();
        Field[] declaredFields = clz.getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            JSONObjectPath annotation = field.getAnnotation(JSONObjectPath.class);
            if (annotation == null) {
                continue;
            }
            JSONObjectUtils.putValue(jsonObject, annotation.path(), annotation.sep(), field.get(object));
        }
        return jsonObject;
    }

}
