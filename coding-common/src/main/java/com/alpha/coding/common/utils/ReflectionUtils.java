package com.alpha.coding.common.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * ReflectionUtils
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
@Deprecated
public class ReflectionUtils {

    /**
     * 获得泛型类型
     *
     * @param clazz class类型
     * @return 返回首个泛型类型
     */
    public static Class<?> getSuperClassGenericType(final Class<?> clazz) {
        return getSuperClassGenericType(clazz, 0);
    }

    /**
     * 获得类泛型类型
     *
     * @param clazz class类型
     * @param index 泛型参数位置
     * @return 返回指定位置的泛型类型
     */
    public static Class<?> getSuperClassGenericType(final Class<?> clazz, final int index) {
        Type genType = clazz.getGenericSuperclass();

        if (!(genType instanceof ParameterizedType)) {
            log.warn(clazz.getSimpleName() + "'s superclass not ParameterizedType");
            return Object.class;
        }

        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

        if (index >= params.length || index < 0) {
            log.warn("Index: " + index + ", Size of " + clazz.getSimpleName() + "'s Parameterized Type: "
                    + params.length);
            return Object.class;
        }
        if (!(params[index] instanceof Class)) {
            log.warn(clazz.getSimpleName() + " not set the actual class on superclass generic parameter");
            return Object.class;
        }

        return (Class<?>) params[index];
    }

    /**
     * 获取classs属性的get方法
     *
     * @param c            class
     * @param propertyName 属性名
     * @return get Method
     */
    public static Method getGetterMethod(Class<?> c, String propertyName) {
        try {
            return c.getMethod("get" + initStr(propertyName));
        } catch (Exception e) {
            throw new RuntimeException("getter not found");
        }
    }

    /**
     * 首字母变大写
     *
     * @param string
     * @return
     */
    private static String initStr(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    /**
     * 获取所有Field
     */
    public static List<Field> getAllFields(Class clz) {
        List<Field> fields = new ArrayList<>();
        Class tmp = clz;
        while (tmp != null && !tmp.equals(Object.class)) {
            fields.addAll(Arrays.asList(tmp.getDeclaredFields()));
            tmp = tmp.getSuperclass();
        }
        return fields;
    }

    /**
     * Make the given field accessible, explicitly setting it accessible if
     * necessary. The {@code setAccessible(true)} method is only called
     * when actually necessary, to avoid unnecessary conflicts with a JVM
     * SecurityManager (if active).
     *
     * @param field the field to make accessible
     * @see java.lang.reflect.Field#setAccessible
     */
    public static void makeAccessible(Field field) {
        if ((!Modifier.isPublic(field.getModifiers()) ||
                     !Modifier.isPublic(field.getDeclaringClass().getModifiers()) ||
                     Modifier.isFinal(field.getModifiers())) && !field.isAccessible()) {
            field.setAccessible(true);
        }
    }

    /**
     * Make the given method accessible, explicitly setting it accessible if
     * necessary. The {@code setAccessible(true)} method is only called
     * when actually necessary, to avoid unnecessary conflicts with a JVM
     * SecurityManager (if active).
     *
     * @param method the method to make accessible
     * @see java.lang.reflect.Method#setAccessible
     */
    public static void makeAccessible(Method method) {
        if ((!Modifier.isPublic(method.getModifiers()) ||
                     !Modifier.isPublic(method.getDeclaringClass().getModifiers())) && !method.isAccessible()) {
            method.setAccessible(true);
        }
    }

}
