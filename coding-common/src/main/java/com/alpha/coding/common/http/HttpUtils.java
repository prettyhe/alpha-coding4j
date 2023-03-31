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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alpha.coding.bo.function.common.Predicates;
import com.alpha.coding.common.http.model.HttpParamControl;

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
     * 基本类型
     */
    private static final List<Type> PRIMITIVE_TYPES = Arrays.asList(Character.class, char.class, BigDecimal.class,
            Short.class, short.class, Integer.class, int.class, Long.class, long.class, Byte.class, byte.class,
            Float.class, float.class, Double.class, double.class, Boolean.class, boolean.class, String.class,
            Date.class, java.sql.Date.class, Timestamp.class, java.sql.Time.class, LocalDate.class, LocalTime.class,
            LocalDateTime.class, BigInteger.class);
    /**
     * 数值型时间格式
     */
    private static final List<String> NUMERIC_AUTO_FORMATS = Arrays.asList("yyyyMMddHHmmssSSS", "yyyyMMddHHmmss",
            "yyyyMMddHH", "yyyyMMdd");
    /**
     * 文本型时间格式
     */
    private static final List<String> STRING_AUTO_FORMATS = Arrays.asList("yyyy-MM-dd HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd HH", "yyyy-MM-dd", "dd/MM/yyyy", "yyyy/MM/dd");
    /**
     * GMT时区
     */
    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");

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
            throw new IllegalArgumentException("FailedToEncodeUri", e);
        }
    }

    /**
     * Decode a URL segment with special chars replaced.
     */
    public static String urlDecode(String value, String encoding) {
        if (value == null) {
            return "";
        }
        try {
            String decode = value.replace("%20", "+")
                    .replace("%2A", "*")
                    .replace("%7E", "~")
                    .replace("%2F", "/");
            return URLDecoder.decode(decode, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("FailedToDecodeUri", e);
        }
    }

    /**
     * 将请求解析成JSONObject
     */
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
            if (Predicates.isNotEmptyMap.test(valueMap)) {
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

    /**
     * 提取参数
     */
    public static Object parseParams(HttpServletRequest request, HttpParamControl httpParamControl) throws IOException {
        Object obj = null;
        // 先从parameter取
        final Enumeration<String> parameterNames = request.getParameterNames();
        final Class<?> parameterType = httpParamControl.getParameterType();
        final Type genericParameterType = httpParamControl.getGenericParameterType();
        if (parameterNames != null && parameterNames.hasMoreElements()) {
            for (String nameCandidate : httpParamControl.getParameterNameCandidates()) {
                try {
                    final String parameter = request.getParameter(nameCandidate);
                    if (parameter == null) {
                        continue;
                    }
                    obj = convertFromString(parameter, httpParamControl);
                    if (obj != null) {
                        return obj;
                    }
                } catch (Exception e) {
                    if (log.isTraceEnabled()) {
                        log.debug("parseFromParameter-fail, name: {}, nameCandidate: {}, type: {}, msg: {}",
                                httpParamControl.getParameterName(), nameCandidate,
                                genericParameterType.getTypeName(), e.getMessage());
                    }
                }
            }
            // 非基本类型
            if (!isPrimitiveType(parameterType)) {
                try {
                    final Map<String, String> parameters = getParameters(request);
                    obj = JSON.parseObject(JSON.toJSONString(parameters), genericParameterType);
                    if (obj != null) {
                        return obj;
                    }
                } catch (Exception e) {
                    if (log.isTraceEnabled()) {
                        log.debug("parseNonPrimitiveFromParameter-fail, name: {}, type: {}, msg: {}",
                                httpParamControl.getParameterName(), genericParameterType.getTypeName(),
                                e.getMessage());
                    }
                }
            }
        }
        // 再从ServletInputStream取
        if (!HttpParameterUtils.isFormContentType(request)) {
            try {
                final String characterEncoding = getCharacterEncoding(request);
                final ServletInputStream inputStream = request.getInputStream();
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
                if (bytes.length != 0) {
                    final String content = new String(bytes, characterEncoding);
                    if (isPrimitiveType(parameterType) && !isJsonObject(content) && !isJsonArray(content)) {
                        try {
                            obj = convertFromString(content, httpParamControl);
                            if (obj != null) {
                                return obj;
                            }
                        } catch (Exception e) {
                            if (log.isTraceEnabled()) {
                                log.debug("parsePrimitiveFromBody-fail, name: {}, type: {}, msg: {}",
                                        httpParamControl.getParameterName(), genericParameterType.getTypeName(),
                                        e.getMessage());
                            }
                        }
                    } else if (!isPrimitiveType(parameterType)) {
                        try {
                            obj = JSON.parseObject(content, genericParameterType);
                            if (obj != null) {
                                return obj;
                            }
                        } catch (Exception e) {
                            if (log.isTraceEnabled()) {
                                log.debug("parseNonPrimitiveFromBody-fail, name: {}, type: {}, msg: {}",
                                        httpParamControl.getParameterName(), genericParameterType.getTypeName(),
                                        e.getMessage());
                            }
                        }
                    }
                }
            } finally {
                if (log.isDebugEnabled()) {
                    log.debug("parseParams: {}, type: {}, content-length: {}",
                            JSON.toJSONString(obj), genericParameterType.getTypeName(),
                            request.getContentLength());
                }
            }
        }
        if (!httpParamControl.isRequired()) {
            if (char.class == parameterType) {
                return (char) 0;
            } else if (byte.class == parameterType) {
                return (byte) 0;
            } else if (short.class == parameterType) {
                return (short) 0;
            } else if (int.class == parameterType) {
                return 0;
            } else if (long.class == parameterType) {
                return (long) 0;
            } else if (float.class == parameterType) {
                return (float) 0;
            } else if (double.class == parameterType) {
                return (double) 0;
            } else if (boolean.class == parameterType) {
                return false;
            }
        }
        return obj;
    }

    private static boolean isJsonObject(CharSequence charSequence) {
        return charSequence.charAt(0) == '{' && charSequence.charAt(charSequence.length() - 1) == '}';
    }

    private static boolean isJsonArray(CharSequence charSequence) {
        return charSequence.charAt(0) == '[' && charSequence.charAt(charSequence.length() - 1) == ']';
    }

    /**
     * 是否是基本类型
     */
    public static boolean isPrimitiveType(Type type) {
        return PRIMITIVE_TYPES.contains(type);
    }

    /**
     * 转换
     */
    public static Object convertFromString(String content, HttpParamControl httpParamControl) {
        final Class<?> parameterType = httpParamControl.getParameterType();
        if (isPrimitiveType(parameterType)) {
            // 基础类型转换
            if (!isJsonObject(content) && !isJsonArray(content)) {
                if (Date.class.isAssignableFrom(parameterType) || LocalDate.class.equals(parameterType)
                        || LocalTime.class.equals(parameterType) || LocalDateTime.class.equals(parameterType)) {
                    return parseDate(content, parameterType, httpParamControl.getDateFormatCandidate());
                }
                return JSON.parseObject(content, parameterType);
            }
        } else {
            // 非基础类型转换
            return JSON.parseObject(content, httpParamControl.getGenericParameterType());
        }
        return null;
    }

    /**
     * 解析成时间
     */
    private static Object parseDate(String str, Type type, String[] dateFormatCandidates) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        final Date date = smartParse(str, dateFormatCandidates, null);
        if (date == null) {
            return null;
        }
        if (type == java.sql.Date.class) {
            return new java.sql.Date(date.getTime());
        } else if (type == java.sql.Timestamp.class) {
            return new java.sql.Timestamp(date.getTime());
        } else if (type == java.sql.Time.class) {
            return new java.sql.Time(date.getTime());
        } else if (type == LocalDate.class) {
            Instant instant = date.toInstant();
            ZoneId zoneId = ZoneId.systemDefault();
            return instant.atZone(zoneId).toLocalDate();
        } else if (type == LocalTime.class) {
            Instant instant = date.toInstant();
            ZoneId zoneId = ZoneId.systemDefault();
            return instant.atZone(zoneId).toLocalTime();
        } else if (type == LocalDateTime.class) {
            Instant instant = date.toInstant();
            ZoneId zoneId = ZoneId.systemDefault();
            return instant.atZone(zoneId).toLocalDateTime();
        } else {
            return date;
        }
    }

    /**
     * 是否是数值类型
     */
    private static boolean isNumeric(String str) {
        if (str == null) {
            return false;
        } else {
            int sz = str.length();
            for (int i = 0; i < sz; ++i) {
                if (!Character.isDigit(str.charAt(i))) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * smartParse 智能解析(尝试按目标格式以及预定义格式解析)
     *
     * @param dateStr              时间字符串
     * @param dateFormatCandidates 目标格式候选
     * @param timezoneOffsetMillis 目标时区与标准时区的时间差(毫秒)
     * @return java.util.Date
     */
    private static Date smartParse(String dateStr, String[] dateFormatCandidates, Long timezoneOffsetMillis) {
        if (dateStr == null || dateStr.length() == 0) {
            return null;
        }
        TimeZone timeZone = timezoneOffsetMillis == null ? null : GMT;
        Date date = null;
        if (dateFormatCandidates != null) {
            date = multiParse(dateStr, Arrays.asList(dateFormatCandidates), timeZone);
        }
        if (date == null && isNumeric(dateStr)
                && dateStr.length() == String.valueOf(System.currentTimeMillis()).length()) {
            date = new Date(Long.parseLong(dateStr));
        }
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
    private static Date multiParse(String dateStr, Collection<String> formats, TimeZone timeZone) {
        for (String format : formats) {
            if (format == null) {
                continue;
            }
            try {
                if (dateStr.length() != format.length()) {
                    continue;
                }
                final SimpleDateFormat dateFormat = new SimpleDateFormat(format);
                if (timeZone != null) {
                    dateFormat.setTimeZone(timeZone);
                }
                Date date = dateFormat.parse(dateStr);
                if (date != null) {
                    return date;
                }
            } catch (Exception e) {
                // nothing
            }
        }
        return null;
    }

    public static Map<String, String> getParameters(HttpServletRequest request) {
        final Map<String, String> map = new HashMap<>();
        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames != null && parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = request.getParameter(paramName);
            map.put(paramName, paramValue);
        }
        return map;
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

    /**
     * 获取domain
     */
    public static String getDomain(HttpServletRequest request) {
        StringBuffer url = request.getRequestURL();
        return url.delete(url.length() - request.getRequestURI().length(), url.length()).toString();
    }

    /**
     * 获取path
     */
    public static String getPath(HttpServletRequest request) {
        return request.getRequestURI().substring(request.getContextPath().length());
    }

    /**
     * 获取HttpServletRequest的编码，默认使用UTF-8
     */
    public static String getCharacterEncoding(HttpServletRequest request) {
        ServletServerHttpRequest httpRequest = new ServletServerHttpRequest(request);
        String charset = null;
        MediaType contentType = httpRequest.getHeaders().getContentType();
        if (contentType != null) {
            final Charset contentTypeCharset = contentType.getCharset();
            if (contentTypeCharset != null) {
                charset = contentTypeCharset.name();
            }
        }
        if (charset == null) {
            charset = request.getCharacterEncoding();
        }
        if (charset == null) {
            charset = StandardCharsets.UTF_8.name();
        }
        return charset;
    }

    /**
     * 从表单中读取值
     */
    public static MultiValueMap<String, String> readBodyFormParams(HttpServletRequest request) throws IOException {
        if (request.getContentLength() <= 0) {
            return new LinkedMultiValueMap<>(0);
        }
        final String charset = getCharacterEncoding(request);
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

    /**
     * 读取QueryString
     */
    public static MultiValueMap<String, String> readQueryStringParams(String str, Charset charset)
            throws UnsupportedEncodingException {
        return readQueryStringParams(str, charset.name());
    }

    /**
     * 读取QueryString
     */
    public static MultiValueMap<String, String> readQueryStringParams(String str, String charsetName)
            throws UnsupportedEncodingException {
        if (str == null) {
            return new LinkedMultiValueMap<>(0);
        }
        String[] pairs = StringUtils.tokenizeToStringArray(str, "&");
        MultiValueMap<String, String> result = new LinkedMultiValueMap<>(pairs.length);
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx == -1) {
                result.add(URLDecoder.decode(pair, charsetName), null);
            } else {
                String name = URLDecoder.decode(pair.substring(0, idx), charsetName);
                String value = URLDecoder.decode(pair.substring(idx + 1), charsetName);
                result.add(name, value);
            }
        }
        return result;
    }

    /**
     * 获取所有header
     */
    public static Map<String, List<String>> getAllHeaders(HttpServletRequest request) {
        final Map<String, List<String>> map = new HashMap<>();
        final Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            final String headerName = headerNames.nextElement();
            final Enumeration<String> headers = request.getHeaders(headerName);
            final List<String> headerValues = new ArrayList<>();
            while (headers.hasMoreElements()) {
                headerValues.add(headers.nextElement());
            }
            map.put(headerName, Collections.unmodifiableList(headerValues));
        }
        return map;
    }

    /**
     * 获取所有header
     */
    public static Map<String, String> getAllHeaderValues(HttpServletRequest request) {
        final Map<String, String> map = new HashMap<>();
        final Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            final String headerName = headerNames.nextElement();
            map.put(headerName, request.getHeader(headerName));
        }
        return map;
    }

}
