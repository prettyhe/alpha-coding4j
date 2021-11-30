package com.alpha.coding.common.utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.extern.slf4j.Slf4j;

/**
 * JacksonUtils
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class JacksonUtils {

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 反序列化为object，异常则返回null
     *
     * @param json  json字符串
     * @param clazz Object类型
     *
     * @return 反序列化的结果
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            if (json == null) {
                return null;
            }
            return fromJsonWithException(json, clazz);
        } catch (Exception e) {
            log.warn("deserialize from json fail: {}", json, e);
            return null;
        }
    }

    /**
     * 反序列化为Object
     *
     * @param json          json字符串
     * @param typeReference 类型
     *
     * @return 反序列化的结果
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) throws JsonProcessingException {
        return json == null ? null : objectMapper.readValue(json, typeReference);
    }

    /**
     * 反序列化为object
     *
     * @param json json字符串
     * @param c    Object类型
     * @param t    Object泛型类型
     *
     * @return 反序列化的结果
     *
     * @throws JacksonException
     */
    public static <T> T fromJson(String json, Class<T> c, Class<?>... t) throws JacksonException {
        try {
            if (json == null) {
                return null;
            }
            return fromJsonWithException(json, c, t);
        } catch (IOException e) {
            throw new JacksonException(e);
        }
    }

    /**
     * 反序列化为object
     *
     * @param json  json字符串
     * @param clazz Object类型
     *
     * @return 反序列化的结果
     */
    public static <T> T fromJsonWithException(String json, Class<T> clazz)
            throws JsonParseException, JsonMappingException, IOException {
        return objectMapper.readValue(json, clazz);
    }

    /**
     * 反序列化为object
     *
     * @param json json字符串
     * @param c    Object类型
     * @param t    Object泛型类型
     *
     * @return 反序列化的结果
     *
     * @throws JsonParseException,JsonMappingException,IOException
     */
    public static <T> T fromJsonWithException(String json, Class<T> c, Class<?>... t)
            throws JsonParseException, JsonMappingException, IOException {
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(c, t);
        return objectMapper.readValue(json, javaType);
    }

    /**
     * 反序列化为List,异常返回null
     *
     * @param json json字符串
     * @param t    List泛型类型
     *
     * @return 反序列化的list
     */
    public static <T> List<T> fromJsonList(String json, Class<T> t) {
        try {
            if (StringUtils.isBlank(json)) {
                return null;
            }
            return fromJsonListWithException(json, t);
        } catch (Exception e) {
            log.warn("deserialize to list from json fail, json={}", json, e);
            return null;
        }
    }

    public static <T> List<T> fromJsonListWithException(String json, Class<T> c) throws JacksonException {
        try {
            JavaType type = getCollectionType(ArrayList.class, c);
            return (List<T>) objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new JacksonException(e);
        }
    }

    public static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
        return objectMapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }

    public static String toJsonWithException(Object o) throws JsonProcessingException {
        return objectMapper.writeValueAsString(o);
    }

    /**
     * object序列化为json字符串
     */
    public static String toJson(Object o) {
        if (o == null) {
            return null;
        }
        try {
            return toJsonWithException(o);
        } catch (Exception e) {
            throw new JacksonException(e);
        }
    }

    public static Map<String, Object> jsonToMap(String json) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            map = objectMapper.readValue(json, HashMap.class);
        } catch (Exception e) {
            log.warn("json to map failed", e);
        }
        return map;
    }

    public static <T, K> Map<T, K> convertValue(Object req, Class<T> keyClazz, Class<K> valueClazz) {
        Map<T, K> ret = objectMapper.convertValue(req,
                objectMapper.getTypeFactory().constructMapType(Map.class, keyClazz, valueClazz));
        return ret;
    }

    public static <T> T convertMap(Map map, Class<T> retClazz) {
        return map == null ? null : objectMapper.convertValue(map, retClazz);
    }

    public static class JacksonException extends RuntimeException {

        private static final long serialVersionUID = 2419465011453289773L;

        public JacksonException(String msg) {
            super(msg);
        }

        public JacksonException(Throwable cause) {
            super(cause);
        }

        public JacksonException(String msg, Throwable cause) {
            super(msg, cause);
        }

    }

}
