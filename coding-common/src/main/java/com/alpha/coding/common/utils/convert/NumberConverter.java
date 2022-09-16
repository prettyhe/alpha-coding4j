package com.alpha.coding.common.utils.convert;

import static com.alpha.coding.bo.function.common.Converter.convertToNumber;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.beanutils.Converter;

/**
 * NumberConverter
 *
 * @version 1.0
 * Date: 2022/3/22
 */
public class NumberConverter implements Converter {

    private static final List<Class<?>> PRIMITIVE_NUMBER_TYPES = Arrays.asList(byte.class, short.class,
            int.class, long.class, float.class, double.class);

    private static final NumberConverter INSTANCE = new NumberConverter();

    public static NumberConverter getDefault() {
        return INSTANCE;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <T> T convert(Class<T> type, Object value) {
        if (type == null || (!PRIMITIVE_NUMBER_TYPES.contains(type) && !Number.class.isAssignableFrom(type))) {
            throw new IllegalArgumentException("unknown number type: " + (type == null ? null : type.getName()));
        }
        return (T) convertToNumber.apply(value, type);
    }

}
