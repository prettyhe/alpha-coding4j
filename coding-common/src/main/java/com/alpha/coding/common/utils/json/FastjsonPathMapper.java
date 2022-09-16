package com.alpha.coding.common.utils.json;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONPath;
import com.alpha.coding.bo.annotation.JsonPath;
import com.alpha.coding.common.utils.FieldUtils;

/**
 * FastjsonPathMapper
 *
 * @version 1.0
 * Date: 2022/8/19
 */
public class FastjsonPathMapper {

    /**
     * 提取path对应的值
     *
     * @param rootObject 源对象
     * @param path       JSONPath
     * @param type       目标类型
     * @see JSONPath
     */
    public static Object eval(Object rootObject, String path, Type type) {
        return eval(rootObject, path, type, true);
    }

    /**
     * 提取path对应的值
     *
     * @param rootObject      源对象
     * @param path            JSONPath
     * @param type            目标类型
     * @param ignoreNullValue 是否忽略空值
     * @see JSONPath
     */
    public static Object eval(Object rootObject, String path, Type type, boolean ignoreNullValue) {
        final JSONPath jsonPath = JSONPath.compile(path, ignoreNullValue);
        return jsonPath.eval(rootObject, type);
    }

    /**
     * 字段提取（按JSONPath提取对象中的值）
     *
     * @param rootObject  源对象
     * @param targetClass 类型
     * @see JSONPath
     * @see JsonPath
     */
    public static Map<String, Object> extract(Object rootObject, Class<?> targetClass) {
        if (rootObject == null) {
            return null;
        }
        final Map<String, Object> map = new HashMap<>();
        final List<Field> matchedFields = FieldUtils.findMatchedFields(targetClass, JsonPath.class);
        for (Field field : matchedFields) {
            JsonPath jsonPath = field.getAnnotation(JsonPath.class);
            if (jsonPath != null) {
                map.put(field.getName(),
                        eval(rootObject, jsonPath.path(), field.getGenericType(), jsonPath.ignoreNullValue()));
            }
        }
        return map;
    }

    /**
     * 对象映射（按JSONPath提取对象中的值，转换给目标对象）
     *
     * @param rootObject 源对象
     * @param target     目标对象
     * @see JSONPath
     * @see JsonPath
     */
    public static void mapping(Object rootObject, Object target) {
        if (rootObject == null || target == null) {
            return;
        }
        final List<Field> matchedFields = FieldUtils.findMatchedFields(target.getClass(), JsonPath.class);
        for (Field field : matchedFields) {
            JsonPath jsonPath = field.getAnnotation(JsonPath.class);
            if (jsonPath != null) {
                field.setAccessible(true);
                FieldUtils.setField(target, field.getName(),
                        eval(rootObject, jsonPath.path(), field.getGenericType(), jsonPath.ignoreNullValue()));
            }
        }
    }

}
