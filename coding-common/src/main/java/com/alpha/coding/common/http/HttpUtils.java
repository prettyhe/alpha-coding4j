package com.alpha.coding.common.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import lombok.extern.slf4j.Slf4j;

/**
 * HttpUtils
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class HttpUtils {

    /**
     * Encode request parameters to URL segment.
     */
    public static String paramToQueryString(Map<String, String> params, String charset) {
        if (params == null || params.isEmpty()) {
            return null;
        }
        StringBuilder paramString = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> p : params.entrySet()) {
            String key = p.getKey();
            String value = p.getValue();
            if (!first) {
                paramString.append("&");
            }
            // Urlencode each request parameter
            paramString.append(urlEncode(key, charset));
            if (value != null) {
                paramString.append("=").append(urlEncode(value, charset));
            }
            first = false;
        }
        return paramString.toString();
    }

    /**
     * Encode a URL segment with special chars replaced.
     */
    public static String urlEncode(String value, String encoding) {
        if (value == null) {
            return "";
        }
        try {
            String encoded = URLEncoder.encode(value, encoding);
            return encoded.replace("+", "%20")
                    .replace("*", "%2A")
                    .replace("~", "%7E")
                    .replace("/", "%2F");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("FailedToEncodeUri");
        }
    }

    public static JSONObject parseParams(HttpServletRequest request) throws IOException {
        JSONObject jsonObject = null;
        try {
            ServletInputStream inputStream = request.getInputStream();
            if (inputStream.markSupported()) {
                inputStream.mark(request.getContentLength());
            }
            Object obj = null;
            final byte[] bytes = new byte[request.getContentLength()];
            try {
                inputStream.read(bytes);
            } finally {
                if (inputStream.markSupported()) {
                    inputStream.reset();
                }
            }
            obj = JSON.parseObject(bytes, 0, request.getContentLength(), StandardCharsets.UTF_8, Object.class);
            if (obj == null) {
                jsonObject = new JSONObject();
            } else {
                jsonObject = obj instanceof JSONObject ? (JSONObject) obj : JSON.parseObject(JSON.toJSONString(obj));
            }
        } catch (Exception e) {
            log.warn("parseParams fail from body, msg is {}", e.getMessage());
            final MultiValueMap<String, String> valueMap = readBodyFormParams(request);
            if (valueMap != null && !valueMap.isEmpty()) {
                jsonObject = JSON.parseObject(JSON.toJSONString(valueMap.toSingleValueMap()));
            }
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("parseParams: {}, content-length: {}",
                        JSON.toJSONString(jsonObject), request.getContentLength());
            }
        }
        return jsonObject;
    }

    public static Object parseParams(HttpServletRequest request, Type type) throws IOException {
        return parseParams(request, type, null);
    }

    public static Object parseParams(HttpServletRequest request, Type type, String[] names) throws IOException {
        Object obj = null;
        try {
            ServletInputStream inputStream = request.getInputStream();
            if (inputStream.markSupported()) {
                inputStream.mark(request.getContentLength());
            }
            final byte[] bytes = new byte[request.getContentLength()];
            try {
                inputStream.read(bytes);
            } finally {
                if (inputStream.markSupported()) {
                    inputStream.reset();
                }
            }
            try {
                final Object primitive = parsePrimitive(bytes, type, names);
                if (primitive != null) {
                    return primitive;
                }
            } catch (Exception e) {
                if (log.isTraceEnabled()) {
                    log.debug("parsePrimitive-fail, type: {}, names: {}, msg: {}",
                            type.getTypeName(), Arrays.toString(names), e.getMessage());
                }
            }
            obj = JSONObject.parseObject(bytes, 0, bytes.length, StandardCharsets.UTF_8, type);
        } catch (Exception e) {
            if (log.isTraceEnabled()) {
                log.debug("parseParams-fail, type: {}, names: {}, msg: {}",
                        type.getTypeName(), Arrays.toString(names), e.getMessage());
            }
            final MultiValueMap<String, String> valueMap = readBodyFormParams(request);
            if (valueMap != null && !valueMap.isEmpty()) {
                obj = JSON.parseObject(JSON.toJSONString(valueMap.toSingleValueMap()), type);
            }
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("parseParams: {}, type: {}, content-length: {}",
                        JSON.toJSONString(obj), type.getTypeName(), request.getContentLength());
            }
        }
        return obj;
    }

    private static Object parsePrimitive(final byte[] bytes, Type type, String[] names) {
        if (bytes == null || bytes.length == 0 || names == null || names.length == 0) {
            return null;
        }
        if (Character.class.equals(type) || char.class.equals(type)
                || BigDecimal.class.equals(type)
                || Short.class.equals(type) || short.class.equals(type)
                || Integer.class.equals(type) || int.class.equals(type)
                || Long.class.equals(type) || long.class.equals(type)
                || Float.class.equals(type) || float.class.equals(type)
                || Double.class.equals(type) || double.class.equals(type)
                || Byte.class.equals(type) || byte.class.equals(type)
                || Boolean.class.equals(type) || boolean.class.equals(type)
                || String.class.equals(type)
                || Date.class.equals(type)
                || BigInteger.class.equals(type)) {
            final JSONObject jsonObject = JSON.parseObject(new String(bytes, StandardCharsets.UTF_8));
            Function<Function<String, Object>, Object> function = f -> Arrays.stream(names)
                    .map(x -> {
                        try {
                            return f.apply(x);
                        } catch (Exception e) {
                            if (log.isDebugEnabled()) {
                                log.debug("parsePrimitive-invoke-fail, x: {}, type: {}, msg: {}",
                                        x, type.getTypeName(), e.getMessage());
                            }
                            return null;
                        }
                    }).filter(Objects::nonNull).findFirst().orElse(null);
            if (Character.class.equals(type) || char.class.equals(type)) {
                return function.apply(s -> jsonObject.getString(s).charAt(0));
            } else if (BigDecimal.class.equals(type)) {
                return function.apply(jsonObject::getBigDecimal);
            } else if (Short.class.equals(type)) {
                return function.apply(jsonObject::getShort);
            } else if (short.class.equals(type)) {
                return function.apply(jsonObject::getShortValue);
            } else if (Integer.class.equals(type)) {
                return function.apply(jsonObject::getInteger);
            } else if (int.class.equals(type)) {
                return function.apply(jsonObject::getIntValue);
            } else if (Long.class.equals(type)) {
                return function.apply(jsonObject::getLong);
            } else if (long.class.equals(type)) {
                return function.apply(jsonObject::getLongValue);
            } else if (Float.class.equals(type)) {
                return function.apply(jsonObject::getFloat);
            } else if (float.class.equals(type)) {
                return function.apply(jsonObject::getFloatValue);
            } else if (Double.class.equals(type)) {
                return function.apply(jsonObject::getDouble);
            } else if (double.class.equals(type)) {
                return function.apply(jsonObject::getDoubleValue);
            } else if (Byte.class.equals(type)) {
                return function.apply(jsonObject::getByte);
            } else if (byte.class.equals(type)) {
                return function.apply(jsonObject::getByteValue);
            } else if (Boolean.class.equals(type)) {
                return function.apply(jsonObject::getBoolean);
            } else if (boolean.class.equals(type)) {
                return function.apply(jsonObject::getBooleanValue);
            } else if (String.class.equals(type)) {
                return function.apply(jsonObject::getString);
            } else if (Date.class.equals(type)) {
                return function.apply(jsonObject::getDate);
            } else if (BigInteger.class.equals(type)) {
                return function.apply(jsonObject::getBigInteger);
            }
            return null;
        }
        return null;
    }

    public static Map<String, String> getParameters(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        Enumeration<String> enums = request.getParameterNames();
        while (enums.hasMoreElements()) {
            String paramName = enums.nextElement();
            String paramValue = request.getParameter(paramName);
            map.put(paramName, paramValue);
        }
        return map;
    }

    public static String getDomain(HttpServletRequest request) {
        StringBuffer url = request.getRequestURL();
        return url.delete(url.length() - request.getRequestURI().length(), url.length()).toString();
    }

    public static String mergeUriWithParams(String uri, Map<String, Object> params) {
        String url = null;
        if (params != null && params.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                sb.append(entry.getKey()).append('=').append(entry.getValue()).append('&');
            }
            sb.deleteCharAt(sb.lastIndexOf("&"));
            if (uri.contains("?")) {
                url = uri + "&" + sb.toString();
            } else {
                url = uri + "?" + sb.toString();
            }
        } else {
            url = uri;
        }
        return url;
    }

    public static String getPath(HttpServletRequest request) {
        return request.getRequestURI().substring(request.getContextPath().length());
    }

    public static MultiValueMap<String, String> readBodyFormParams(HttpServletRequest request) throws IOException {
        ServletServerHttpRequest httpRequest = new ServletServerHttpRequest(request);
        MediaType contentType = httpRequest.getHeaders().getContentType();
        Charset charset = (contentType.getCharset() != null ? contentType.getCharset() : StandardCharsets.UTF_8);
        ServletInputStream inputStream = request.getInputStream();
        if (inputStream.markSupported()) {
            inputStream.mark(request.getContentLength());
        }
        final byte[] bytes = new byte[request.getContentLength()];
        try {
            inputStream.read(bytes);
        } finally {
            if (inputStream.markSupported()) {
                inputStream.reset();
            }
        }
        return readQueryStringParams(new String(bytes, charset), charset);
    }

    public static MultiValueMap<String, String> readQueryStringParams(String str, Charset charset)
            throws UnsupportedEncodingException {
        if (str == null) {
            return new LinkedMultiValueMap<>(0);
        }
        String[] pairs = StringUtils.tokenizeToStringArray(str, "&");
        MultiValueMap<String, String> result = new LinkedMultiValueMap<>(pairs.length);
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx == -1) {
                result.add(URLDecoder.decode(pair, charset.name()), null);
            } else {
                String name = URLDecoder.decode(pair.substring(0, idx), charset.name());
                String value = URLDecoder.decode(pair.substring(idx + 1), charset.name());
                result.add(name, value);
            }
        }
        return result;
    }

}
