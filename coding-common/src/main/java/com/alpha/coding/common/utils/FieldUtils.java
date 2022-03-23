package com.alpha.coding.common.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * FieldUtils
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class FieldUtils {

    /**
     * To find out matched {@link Field} marked as {@code ann} annotation
     *
     * @param targetClass taget class
     * @param ann
     * @return found {@link Field} list
     */
    public static List<Field> findMatchedFields(Class targetClass, Class ann) {

        List<Field> ret = new ArrayList<>();
        if (targetClass == null) {
            return ret;
        }

        // Keep backing up the inheritance hierarchy.
        do {
            // Copy each field declared on this class unless it's static or
            // file.
            Field[] fields = targetClass.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Annotation annotation = fields[i].getAnnotation(ann);
                if (annotation != null) {
                    ret.add(fields[i]);
                }
            }
            targetClass = targetClass.getSuperclass();
        } while (targetClass != null && targetClass != Object.class);

        return ret;
    }

    /**
     * Set the field represented by the supplied {@link Field field object} on the specified {@link Object target
     * object} to the specified <code>value</code>. In accordance with {@link Field#set(Object, Object)} semantics, the
     * new value is automatically unwrapped if the underlying field has a primitive type.
     * <p>
     * Thrown exceptions are handled via a call to
     *
     * @param t     the target object on which to set the field
     * @param name  the field to set
     * @param value the value to set; may be <code>null</code>
     */
    public static void setField(Object t, String name, Object value) {
        Field field = findField(t.getClass(), name);
        if (field == null) {
            return;
        }
        field.setAccessible(true);
        try {
            field.set(t, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Attempt to find a {@link Field field} on the supplied {@link Class} with the supplied <code>name</code>. Searches
     * all superclasses up to {@link Object}.
     *
     * @param clazz the class to introspect
     * @param name  the name of the field
     * @return the corresponding Field object, or <code>null</code> if not found
     */
    public static Field findField(Class clazz, String name) {
        return findField(clazz, name, null);
    }

    /**
     * Attempt to find a {@link Field field} on the supplied {@link Class} with the supplied <code>name</code> and/or
     * {@link Class type}. Searches all superclasses up to {@link Object}.
     *
     * @param clazz the class to introspect
     * @param name  the name of the field (may be <code>null</code> if type is specified)
     * @param type  the type of the field (may be <code>null</code> if name is specified)
     * @return the corresponding Field object, or <code>null</code> if not found
     */
    public static Field findField(Class clazz, String name, Class type) {
        if (clazz == null) {
            throw new IllegalArgumentException("Class must not be null");
        }
        if (name == null && type == null) {
            throw new IllegalArgumentException(
                    "Either name or type of the field must be specified");
        }
        Class searchType = clazz;
        while (!Object.class.equals(searchType) && searchType != null) {
            Field[] fields = searchType.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                if ((name == null || name.equals(field.getName()))
                        && (type == null || type.equals(field.getType()))) {
                    return field;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

}
