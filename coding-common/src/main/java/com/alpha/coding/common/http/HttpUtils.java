package com.alpha.coding.common.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
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
            try {
                obj = JSON.parseObject(inputStream, Charset.forName("UTF-8"), Object.class);
            } finally {
                if (inputStream.markSupported()) {
                    inputStream.reset();
                }
            }
            if (obj == null) {
                jsonObject = new JSONObject();
            } else {
                jsonObject = obj instanceof JSONObject ? (JSONObject) obj : JSON.parseObject(JSON.toJSONString(obj));
            }
        } catch (Exception e) {
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
        Object obj = null;
        try {
            ServletInputStream inputStream = request.getInputStream();
            if (inputStream.markSupported()) {
                inputStream.mark(request.getContentLength());
            }
            try {
                obj = JSONObject.parseObject(inputStream, Charset.forName("UTF-8"), type);
            } finally {
                if (inputStream.markSupported()) {
                    inputStream.reset();
                }
            }
        } catch (Exception e) {
            final MultiValueMap<String, String> valueMap = readBodyFormParams(request);
            if (valueMap != null && !valueMap.isEmpty()) {
                obj = JSON.parseObject(JSON.toJSONString(valueMap.toSingleValueMap()), type);
            }
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("parseParams: {}, content-length: {}",
                        JSON.toJSONString(obj), request.getContentLength());
            }
        }
        return obj;
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
        Charset charset = (contentType.getCharset() != null ? contentType.getCharset() : Charset.forName("UTF-8"));
        ServletInputStream inputStream = request.getInputStream();
        if (inputStream.markSupported()) {
            inputStream.mark(request.getContentLength());
        }
        String body;
        try {
            body = StreamUtils.copyToString(inputStream, charset);
        } finally {
            if (inputStream.markSupported()) {
                inputStream.reset();
            }
        }
        return readQueryStringParams(body, charset);
    }

    public static MultiValueMap<String, String> readQueryStringParams(String str, Charset charset)
            throws UnsupportedEncodingException {
        if (str == null) {
            return new LinkedMultiValueMap<>(0);
        }
        String[] pairs = StringUtils.tokenizeToStringArray(str, "&");
        MultiValueMap<String, String> result = new LinkedMultiValueMap<String, String>(pairs.length);
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
