package com.alpha.coding.common.utils.convert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Function;

import org.apache.commons.beanutils.Converter;

import com.alpha.coding.common.utils.DateUtils;

/**
 * DateConverter
 *
 * @version 1.0
 * Date: 2022/3/22
 */
public class DateConverter implements Converter {

    private static Function<Object, Date> defaultConverter = value -> {
        if (value == null) {
            return null;
        }
        if (value instanceof Date) {
            return new Date(((Date) value).getTime());
        }
        if (value instanceof LocalDateTime) {
            return Date.from(((LocalDateTime) value).atZone(ZoneId.systemDefault()).toInstant());
        }
        if (value instanceof LocalDate) {
            return Date.from(((LocalDate) value).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        }
        if (value instanceof LocalTime) {
            return Date.from(((LocalTime) value).atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant());
        }
        if (value instanceof Calendar) {
            return ((Calendar) value).getTime();
        }
        return DateUtils.smartParse(String.valueOf(value));
    };

    public static void resetDefaultConverter(Function<Object, Date> defaultConverter) {
        DateConverter.defaultConverter = defaultConverter;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <T> T convert(Class<T> type, Object value) {
        if (type == null || !Date.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException("unknown date type: " + (type == null ? null : type.getName()));
        }
        return (T) defaultConverter.apply(value);
    }

}
