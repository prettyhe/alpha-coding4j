package com.alpha.coding.bo.json;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alpha.coding.bo.annotation.JsonDecimal;

import lombok.extern.slf4j.Slf4j;

/**
 * DecimalJsonSerializer
 *
 * @version 1.0
 * Date: 2020/12/21
 */
@Slf4j
public class DecimalJsonSerializer implements ObjectSerializer {

    public static final DecimalJsonSerializer INSTANCE = new DecimalJsonSerializer();

    private static final Set<Type> NUMBER_TYPES = new HashSet<>();

    static {
        NUMBER_TYPES.add(byte.class);
        NUMBER_TYPES.add(Byte.class);
        NUMBER_TYPES.add(short.class);
        NUMBER_TYPES.add(Short.class);
        NUMBER_TYPES.add(int.class);
        NUMBER_TYPES.add(Integer.class);
        NUMBER_TYPES.add(long.class);
        NUMBER_TYPES.add(Long.class);
        NUMBER_TYPES.add(float.class);
        NUMBER_TYPES.add(Float.class);
        NUMBER_TYPES.add(double.class);
        NUMBER_TYPES.add(Double.class);
        NUMBER_TYPES.add(BigDecimal.class);
        NUMBER_TYPES.add(BigInteger.class);
        NUMBER_TYPES.add(AtomicInteger.class);
        NUMBER_TYPES.add(AtomicLong.class);
    }

    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features)
            throws IOException {
        JsonDecimal annotation = parseJsonDecimal(serializer.getContext().object, fieldName);
        if (annotation == null || !NUMBER_TYPES.contains(fieldType)) {
            serializer.write(object);
            return;
        }
        BigDecimal target = null;
        if (object != null) {
            if (object instanceof BigDecimal) {
                target = (BigDecimal) object;
            } else {
                final String str = String.valueOf(object);
                if (!str.isEmpty()) {
                    target = new BigDecimal(str);
                }
            }
        }
        if (target == null) {
            if ("null".equals(annotation.defaultValueForNull())) {
                serializer.write(null);
            } else {
                if (annotation.asString()) {
                    serializer.write(annotation.defaultValueForNull());
                } else {
                    serializer.write(convert(new BigDecimal(annotation.defaultValueForNull()), fieldType));
                }
            }
        } else {
            target = target.multiply(new BigDecimal(annotation.multiple()));
            target = target.setScale(annotation.scale(), annotation.roundingMode());
            if (annotation.asString()) {
                serializer.write(target.toPlainString() + annotation.suffixForString());
            } else {
                serializer.write(convert(target, fieldType));
            }
        }
    }

    /**
     * parseJsonDecimal 解析字段上的JsonDecimal注解
     *
     * @param object    目标对象
     * @param fieldName 字段名
     * @return JsonDecimal
     * @see JsonDecimal
     */
    private JsonDecimal parseJsonDecimal(Object object, Object fieldName) {
        try {
            final Field field = object.getClass().getDeclaredField((String) fieldName);
            if (field.isAnnotationPresent(JsonDecimal.class)) {
                return field.getAnnotation(JsonDecimal.class);
            }
        } catch (Exception e) {
            log.warn("parse JsonDecimal fail, fieldName={}, msg={}", fieldName, e.getMessage());
        }
        return null;
    }

    public static <T extends Number> Number convert(T number, Type type) {
        if (number == null) {
            return null;
        }
        if (type == byte.class || type == Byte.class) {
            return number.byteValue();
        } else if (type == short.class || type == Short.class) {
            return number.shortValue();
        } else if (type == int.class || type == Integer.class) {
            return number.intValue();
        } else if (type == long.class || type == Long.class) {
            return number.longValue();
        } else if (type == float.class || type == Float.class) {
            return number.floatValue();
        } else if (type == double.class || type == Double.class) {
            return number.doubleValue();
        } else if (type == BigInteger.class) {
            return new BigInteger(String.valueOf(number));
        } else if (type == BigDecimal.class) {
            return number instanceof BigDecimal ? (BigDecimal) number : new BigDecimal(String.valueOf(number));
        } else if (type == AtomicInteger.class) {
            return new AtomicInteger(number.intValue());
        } else if (type == AtomicLong.class) {
            return new AtomicLong(number.longValue());
        } else {
            return number;
        }
    }

}
