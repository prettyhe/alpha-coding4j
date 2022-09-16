package com.alpha.coding.common.utils.json;

import java.lang.reflect.Field;

import com.alibaba.fastjson.JSONObject;
import com.alpha.coding.bo.annotation.JsonPath;
import com.alpha.coding.common.utils.FieldUtils;

/**
 * JSONObjectGenerator
 *
 * @version 1.0
 */
public class JSONObjectGenerator {

    public static JSONObject generate(Object object) throws IllegalAccessException {
        if (object == null) {
            return null;
        }
        JSONObject jsonObject = new JSONObject();
        Class<?> clz = object.getClass();
        Field[] declaredFields = FieldUtils.findMatchedFields(clz, null).toArray(new Field[0]);
        for (Field field : declaredFields) {
            field.setAccessible(true);
            JSONObjectPath jsonObjectPath = field.getAnnotation(JSONObjectPath.class);
            if (jsonObjectPath != null) {
                JSONObjectUtils.putValue(jsonObject, jsonObjectPath.path(), jsonObjectPath.sep(), field.get(object));
                continue;
            }
            JsonPath jsonPath = field.getAnnotation(JsonPath.class);
            if (jsonPath != null) {
                JSONObjectUtils.putValue(jsonObject, jsonPath.path(), "\\.", field.get(object));
            }
        }
        return jsonObject;
    }

}
