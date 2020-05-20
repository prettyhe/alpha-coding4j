package com.alpha.coding.bo.json;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import javax.validation.constraints.NotNull;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alpha.coding.bo.annotation.JsonFeature;

import lombok.extern.slf4j.Slf4j;

/**
 * OffsetTimeJsonCodec
 *
 * @version 1.0
 * Date: 2020-01-15
 */
@Slf4j
public class OffsetTimeJsonCodec implements ObjectSerializer, ObjectDeserializer {

    private static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private static final List<String> NUMERIC_AUTO_FORMATS = Arrays.asList("yyyyMMddHHmmss",
            "yyyyMMddHH", "yyyyMMdd");
    private static final List<String> STRING_AUTO_FORMATS = Arrays.asList("yyyy-MM-dd HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd HH", "yyyy-MM-dd", "dd/MM/yyyy");

    private static ConcurrentMap<Class<? extends Supplier<Long>>, Supplier<Long>> supplierCache =
            new ConcurrentHashMap<>();

    @Override
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        final Object fieldValue = parser.parse(fieldName);
        if (fieldValue == null) {
            return null;
        }
        final String input = String.valueOf(fieldValue);
        JsonFeature annotation = parseJsonFeature(parser.getContext().object, fieldName);
        if (annotation == null) {
            return (T) smartParse(input, DEFAULT_FORMAT, null);
        } else {
            final Class<? extends Supplier<Long>> clz = annotation.timezoneOffsetSup();
            if (supplierCache.get(clz) == null) {
                try {
                    supplierCache.put(clz, clz.newInstance());
                } catch (Exception e) {
                    throw new RuntimeException(clz.getName() + "can not new instance");
                }
            }
            String format = annotation.timeFormat() == null || annotation.timeFormat().length() == 0 ? DEFAULT_FORMAT :
                    annotation.timeFormat();
            return (T) smartParse(input, format, supplierCache.get(clz).get()); // 使用注解提供的偏移量提供函数得到时差偏移量解析
        }
    }

    @Override
    public int getFastMatchToken() {
        return 0;
    }

    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features)
            throws IOException {
        if (object == null) {
            serializer.write(null);
            return;
        }
        JsonFeature annotation = parseJsonFeature(serializer.getContext().object, fieldName);
        if (annotation == null) {
            serializer.write(formatDate((Date) object, DEFAULT_FORMAT, null));
        } else {
            final Class<? extends Supplier<Long>> clz = annotation.timezoneOffsetSup();
            if (supplierCache.get(clz) == null) {
                try {
                    supplierCache.put(clz, clz.newInstance());
                } catch (Exception e) {
                    throw new IOException(clz.getName() + "can not new instance");
                }
            }
            String format = annotation.timeFormat() == null || annotation.timeFormat().length() == 0 ? DEFAULT_FORMAT :
                    annotation.timeFormat();
            serializer.write(formatDate((Date) object, format, supplierCache.get(clz).get()));
        }
    }

    /**
     * parseJsonFeature 解析字段上的JsonFeature注解
     *
     * @param object    目标对象
     * @param fieldName 字段名
     * @return JsonFeature
     */
    private JsonFeature parseJsonFeature(Object object, Object fieldName) {
        try {
            final Field field = object.getClass().getDeclaredField((String) fieldName);
            if (field.isAnnotationPresent(JsonFeature.class)) {
                return field.getAnnotation(JsonFeature.class);
            }
        } catch (Exception e) {
            log.warn("parse JsonFeature fail, fieldName={}, msg={}", fieldName, e.getMessage());
        }
        return null;
    }

    /**
     * formatDate 格式化时间
     *
     * @param date                 时间
     * @param targetFormat         目标格式
     * @param timezoneOffsetMillis 目标时区与标准时区的时间差(毫秒)
     * @return java.lang.String
     */
    private String formatDate(Date date, String targetFormat, Long timezoneOffsetMillis) {
        String format = targetFormat == null ? DEFAULT_FORMAT : targetFormat;
        if (timezoneOffsetMillis == null) {
            return new SimpleDateFormat(format).format(date);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime() + timezoneOffsetMillis);
        final SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        dateFormat.setTimeZone(GMT);
        return dateFormat.format(calendar.getTime());
    }

    /**
     * smartParse 智能解析(尝试按目标格式以及预定义格式解析)
     *
     * @param dateStr              时间字符串
     * @param targetFormat         目标格式
     * @param timezoneOffsetMillis 目标时区与标准时区的时间差(毫秒)
     * @return java.util.Date
     */
    private Date smartParse(String dateStr, String targetFormat, Long timezoneOffsetMillis) {
        if (dateStr == null || dateStr.length() == 0) {
            return null;
        }
        TimeZone timeZone = timezoneOffsetMillis == null ? null : GMT;
        Date date = multiParse(dateStr, Arrays.asList(targetFormat), timeZone);
        if (date == null) {
            date = multiParse(dateStr, isNumeric(dateStr) ? NUMERIC_AUTO_FORMATS : STRING_AUTO_FORMATS, timeZone);
        }
        if (date == null) {
            return null;
        }
        if (timezoneOffsetMillis == null) {
            return date;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime() - timezoneOffsetMillis);
        return calendar.getTime();
    }

    /**
     * multiParse 多格式解析(尝试用多种格式解析字符串为时间)
     *
     * @param dateStr  时间字符串
     * @param formats  格式
     * @param timeZone 时区
     * @return java.util.Date
     */
    private Date multiParse(String dateStr, @NotNull Collection<String> formats, TimeZone timeZone) {
        for (String format : formats) {
            try {
                final SimpleDateFormat dateFormat = new SimpleDateFormat(format);
                if (timeZone != null) {
                    dateFormat.setTimeZone(timeZone);
                }
                Date date = dateFormat.parse(dateStr);
                if (date != null) {
                    return date;
                }
            } catch (Exception e) {
            }
        }
        return null;
    }

    /**
     * isNumeric 判读一个字符串是否是数字
     *
     * @param str 输入字符串
     * @return boolean
     */
    private static boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        int sz = str.length();
        for (int i = 0; i < sz; i++) {
            if (Character.isDigit(str.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }
}
