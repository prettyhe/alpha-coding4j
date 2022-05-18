package com.alpha.coding.common.http;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.alpha.coding.bo.annotation.HttpRequestParam;
import com.alpha.coding.bo.enums.HttpParamFrom;
import com.alpha.coding.common.utils.ReflectionUtils;
import com.alpha.coding.common.utils.StringUtils;
import com.google.common.collect.Sets;

import lombok.extern.slf4j.Slf4j;

/**
 * HttpParameterUtils
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class HttpParameterUtils {

    public static <T> void parseHttpParameter(HttpServletRequest request, T t) throws IOException {
        if (t == null) {
            return;
        }
        final List<Field> fields = ReflectionUtils.getAllFields(t.getClass()).stream()
                .filter(p -> !Modifier.isStatic(p.getModifiers()) && !Modifier.isFinal(p.getModifiers()))
                .filter(p -> {
                    ReflectionUtils.makeAccessible(p);
                    return p.isAccessible();
                }).collect(Collectors.toList());
        if (fields.size() == 0) {
            return;
        }
        boolean parameterFromForm = false;
        final AtomicReference<MultiValueMap<String, String>> formParametersRef = new AtomicReference<>();
        if (("PUT".equals(request.getMethod()) || "PATCH".equals(request.getMethod())) && isFormContentType(request)) {
            parameterFromForm = true;
            try {
                formParametersRef.set(HttpUtils.readBodyFormParams(request));
                if (log.isDebugEnabled()) {
                    log.debug("formParameters:{}", JSON.toJSONString(formParametersRef.get()));
                }
            } catch (Exception e) {
                log.warn("parse formParameters fail for url={},msg={}",
                        HttpUtils.getPath(request), e.getMessage());
            }
        }
        final AtomicReference<MultiValueMap<String, String>> queryStringParametersRef = new AtomicReference<>();
        try {
            queryStringParametersRef.set(HttpUtils.readQueryStringParams(request.getQueryString(),
                    StandardCharsets.UTF_8));
            if (log.isDebugEnabled()) {
                log.debug("contentType:{}, queryStringParameters:{}",
                        request.getContentType(), JSON.toJSONString(queryStringParametersRef.get()));
            }
        } catch (Exception e) {
            log.warn("parse queryStringParameters fail for url={},msg={}",
                    HttpUtils.getPath(request), e.getMessage());
        }
        boolean excludeFromRaw = true;
        for (Field field : fields) {
            if (field.getType().equals(MultipartFile.class)
                    || MultipartFile.class.isAssignableFrom(field.getType())) {
                continue; // 文件类型的忽略
            }
            final HttpRequestParam an = field.getAnnotation(HttpRequestParam.class);
            if (testInclude(an, HttpParamFrom.Raw)) {
                excludeFromRaw = false;
            }
            if (testInclude(an, HttpParamFrom.Header)) {
                try {
                    setField(t, field, request::getHeader);
                } catch (Exception e) {
                    log.error("ParseHttpParamErr|FromHeader for url={},field={},msg={}",
                            HttpUtils.getPath(request), field.getName(), e.getMessage());
                }
            }
            if (testInclude(an, HttpParamFrom.QueryString)) {
                try {
                    setField(t, field, k -> queryStringParametersRef.get().getFirst(k));
                } catch (Exception e) {
                    log.error("ParseHttpParamErr|FromQueryString for url={},field={},msg={}",
                            HttpUtils.getPath(request), field.getName(), e.getMessage());
                }
            }
            if (testInclude(an, HttpParamFrom.Form)) {
                try {
                    if (parameterFromForm) {
                        setField(t, field, k -> formParametersRef.get().getFirst(k));
                    } else {
                        setField(t, field, request::getParameter);
                    }
                } catch (Exception e) {
                    log.error("ParseHttpParamErr|FromParameter for url={},field={},msg={}",
                            HttpUtils.getPath(request), field.getName(), e.getMessage());
                }
            }
        }
        if (!excludeFromRaw) {
            String contentType = request.getContentType();
            if (contentType == null || !contentType.contains("multipart/form-data")) {
                try {
                    Optional.ofNullable(HttpUtils.parseParams(request)).ifPresent(pas -> {
                        for (Field field : fields) {
                            try {
                                setField(t, field, pas::getString);
                            } catch (Exception e) {
                                log.error("ParseHttpParamErr|FromRaw for {},msg={}",
                                        field.getName(), e.getMessage());
                            }
                        }
                    });
                } catch (Exception e) {
                    log.error("ParseHttpParamErr|FromRaw for url={}, msg={}",
                            HttpUtils.getPath(request), e.getMessage());
                }
            }
        }
    }

    private static void setField(Object obj, Field field, Function<String, String> function)
            throws IllegalAccessException {
        ReflectionUtils.makeAccessible(field);
        final HttpRequestParam an = field.getAnnotation(HttpRequestParam.class);
        final Set<String> names = new LinkedHashSet<>();
        names.add(field.getName());
        if (an != null) {
            Arrays.stream(an.name()).filter(p -> !isBlank(p)).forEach(names::add);
        }
        String value = null;
        for (String name : names) {
            value = function.apply(name);
            if (value != null) {
                break;
            }
        }
        if (value == null) {
            return;
        }
        if (field.getType() == Date.class) {
            StringBuilder sb = new StringBuilder("{");
            Set<String> alternateNames = Sets.newHashSet();
            alternateNames.add(field.getName());
            try {
                if (field.isAnnotationPresent(JSONField.class)) {
                    final JSONField jsonField = field.getAnnotation(JSONField.class);
                    if (StringUtils.isNotBlank(jsonField.name())) {
                        alternateNames.add(jsonField.name());
                    }
                    if (jsonField.alternateNames().length > 0) {
                        alternateNames.addAll(Arrays.asList(jsonField.alternateNames()));
                    }
                }
                for (String name : alternateNames) {
                    sb.append("\"").append(name).append("\":\"").append(value).append("\",");
                }
                sb.deleteCharAt(sb.lastIndexOf(","));
                sb.append("}");
                field.set(obj, field.get(JSON.parseObject(sb.toString(), obj.getClass())));
                return;
            } catch (Exception e) {
                log.error("parseDateFail for {},{}", field.getName(), value);
            }
        }
        field.set(obj, jsonParse(value,
                (an == null || an.javaType() == Object.class) ? field.getType() : an.javaType()));
    }

    private static Object jsonParse(String value, Class<?> clz) {
        if (String.class.equals(clz)) {
            try {
                return JSON.parseObject(value, clz);
            } catch (Exception e) {
                return value;
            }
        }
        return JSON.parseObject(value, clz);
    }

    private static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs != null && (strLen = cs.length()) != 0) {
            for (int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(cs.charAt(i))) {
                    return false;
                }
            }
            return true;
        } else {
            return true;
        }
    }

    private static boolean testInclude(HttpRequestParam an, HttpParamFrom from) {
        return an == null || an.excludeFrom().length == 0
                || Arrays.stream(an.excludeFrom()).noneMatch(p -> p.equals(from));
    }

    private static boolean isFormContentType(HttpServletRequest request) {
        String contentType = request.getContentType();
        if (contentType != null) {
            try {
                MediaType mediaType = MediaType.parseMediaType(contentType);
                return (MediaType.APPLICATION_FORM_URLENCODED.includes(mediaType));
            } catch (IllegalArgumentException ex) {
                return false;
            }
        } else {
            return false;
        }
    }

    public static String appendQueryParam(String url, String key, String value) {
        if (url == null || org.apache.commons.lang3.StringUtils.isBlank(key)) {
            return url;
        }
        if (url.contains("?")) {
            return url + "&" + key + "=" + value;
        } else {
            return url + "?" + key + "=" + value;
        }
    }

}
