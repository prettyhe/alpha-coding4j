package com.alpha.coding.common.http.rest;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alpha.coding.common.http.HttpUtils;
import com.alpha.coding.common.utils.ReflectionUtils;
import com.alpha.coding.common.utils.StringUtils;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * HttpAPIHandler.java
 *
 * @author nick
 * @version 1.0
 * Date: 2018-04-24
 */
@Slf4j
public class HttpAPIHandler implements InvocationHandler, InitializingBean {

    private static final Pattern URI_VARIABLE_PATTERN = Pattern.compile("\\{(.*?)}");

    @Setter
    private MyRestTemplate restTemplate;
    @Setter
    private String uri;
    @Setter
    private UriSelectPolicy selectPolicy = list -> list.get(ThreadLocalRandom.current().nextInt(0, list.size()));

    private List<String> uriList;

    public HttpAPIHandler(MyRestTemplate restTemplate, String uri) {
        this.restTemplate = restTemplate;
        this.uri = uri;
        init();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }

    public void init() {
        if (uri != null) {
            uriList = Arrays.stream(uri.split(","))
                    .map(String::trim).filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
        }
    }

    private String selectUri() {
        if (uriList == null) {
            init();
        }
        return selectPolicy.select(uriList);
    }

    private String contactPath(String path) {
        final String uri = selectUri();
        if (uri.endsWith("/") && path.startsWith("/")) {
            return uri + path.substring(1);
        } else if (uri.endsWith("/") || path.startsWith("/")) {
            return uri + path;
        } else {
            return uri + "/" + path;
        }
    }

    private static List<String> parseUriVariables(String url) {
        List<String> list = new ArrayList<>();
        final Matcher matcher = URI_VARIABLE_PATTERN.matcher(url);
        while (matcher.find()) {
            final String group = matcher.group();
            list.add(group.substring(1, group.length() - 1));
        }
        return list;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass().equals(Object.class)) {
            if (method.getName().equals("hashCode")) {
                return method.hashCode();
            }
            if (method.getName().equals("toString")) {
                return method.toString();
            }
            throw new UnsupportedOperationException("method name: " + method.getName());
        }

        RequestMapping[] requestMappings = method.getAnnotationsByType(RequestMapping.class);
        if (requestMappings == null || requestMappings.length == 0) {
            throw new UnsupportedOperationException("method must have annotation RequestMapping");
        }
        String path = requestMappings[0].value()[0];
        final RequestMethod[] methods = requestMappings[0].method();
        RequestMethod httpMethod = (methods == null || methods.length == 0) ? RequestMethod.POST : methods[0];

        String url = contactPath(path);
        final Map<String, String[]> headerMap = new HashMap<>();
        final Map<String, Object> urlParams = new HashMap<>();
        if (args != null && args.length > 0) {
            final Parameter[] parameters = method.getParameters();
            // 拼装uriVariables参数
            final List<String> variables = parseUriVariables(url);
            if (!variables.isEmpty()) {
                Map<String, Object> paramsMap = new HashMap<>();
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof ServletRequest || args[i] instanceof ServletResponse) {
                        continue;
                    }
                    if (!parameters[i].isAnnotationPresent(PathVariable.class)) {
                        continue;
                    }
                    final PathVariable annotation = parameters[i].getAnnotation(PathVariable.class);
                    if (!annotation.name().isEmpty()) {
                        paramsMap.put(annotation.name(), args[i]);
                    } else {
                        paramsMap.put(parameters[i].getName(), args[i]);
                    }
                }
                for (String key : paramsMap.keySet()) {
                    if (paramsMap.get(key) == null) {
                        continue;
                    }
                    url = url.replaceAll("\\{" + key + "\\}", String.valueOf(paramsMap.get(key)));
                }
            }
            // 拼装url参数
            for (int i = 0; i < args.length; i++) {
                final Object arg = args[i];
                if (arg == null) {
                    continue;
                }
                if (arg instanceof ServletRequest || arg instanceof ServletResponse) {
                    continue;
                }
                final Parameter parameter = parameters[i];
                if (!parameter.isAnnotationPresent(RequestParam.class)) {
                    if (parameter.isAnnotationPresent(RequestBody.class)
                            || parameter.isAnnotationPresent(PathVariable.class)
                            || parameter.isAnnotationPresent(RequestHeader.class)
                            || parameter.isAnnotationPresent(CookieValue.class)) {
                        continue;
                    } else {
                        if (parameter.getType().isPrimitive() || arg.getClass().isPrimitive()
                                || arg instanceof String || arg instanceof Number) {
                            urlParams.put(parameter.getName(), arg);
                        } else if (parameter.getType().isArray()
                                || Collection.class.isAssignableFrom(parameter.getType())) {
                            urlParams.put(parameter.getName() + "[]", JSON.toJSONString(arg));
                        } else {
                            try {
                                Map<String, Object> extraParams = new HashMap<>();
                                expendObjectToUrlParams(arg, extraParams);
                                urlParams.putAll(extraParams);
                            } catch (IllegalAccessException e) {
                                throw e;
                            }
                        }
                    }
                } else {
                    final RequestParam annotation = parameter.getAnnotation(RequestParam.class);
                    String paramName = !annotation.name().isEmpty() ? annotation.name() : parameter.getName();
                    if (parameter.getType().isArray()
                            || Collection.class.isAssignableFrom(parameter.getType())) {
                        urlParams.put(paramName.endsWith("[]") ? paramName : paramName + "[]",
                                JSON.toJSONString(arg, SerializerFeature.DisableCircularReferenceDetect));
                    } else {
                        urlParams.put(paramName, arg);
                    }
                }
            }
            url = HttpUtils.mergeUriWithParams(url, urlParams);
            // 拼装Header
            BiConsumer<String, Object> updateHeader = (k, v) -> {
                if (v == null) {
                    return;
                }
                if (headerMap.get(k) == null) {
                    headerMap.put(k, new String[] {String.valueOf(v)});
                } else {
                    final String[] old = headerMap.get(k);
                    String[] arr = new String[old.length + 1];
                    System.arraycopy(old, 0, arr, 0, old.length);
                    arr[arr.length - 1] = String.valueOf(v);
                    headerMap.put(k, arr);
                }
            };
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof ServletRequest || args[i] instanceof ServletResponse) {
                    continue;
                }
                if (!parameters[i].isAnnotationPresent(RequestHeader.class)) {
                    continue;
                }
                final RequestHeader annotation = parameters[i].getAnnotation(RequestHeader.class);
                if (!annotation.name().isEmpty()) {
                    updateHeader.accept(annotation.name(), args[i]);
                } else {
                    updateHeader.accept(parameters[i].getName(), args[i]);
                }
            }
        }
        final long st = System.currentTimeMillis();
        try {
            if (RequestMethod.GET.equals(httpMethod)) {
                return restTemplate.getForObjectGeneric(url, method.getGenericReturnType(), method.getReturnType(),
                        headerMap);
            } else if (RequestMethod.HEAD.equals(httpMethod)) {
                return restTemplate.headForHeaders(url, (Map<String, ?>) null);
            } else if (RequestMethod.POST.equals(httpMethod)) {
                if (args == null || args.length == 0) {
                    return restTemplate.postForObjectGeneric(url, null, method.getGenericReturnType(),
                            method.getReturnType(), headerMap);
                }
                Object body = null;
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof ServletRequest || args[i] instanceof ServletResponse) {
                        continue;
                    }
                    final Parameter parameter = method.getParameters()[i];
                    if (!parameter.isAnnotationPresent(RequestBody.class)) {
                        continue;
                    }
                    body = args[i];
                    break;
                }
                return restTemplate.postForObjectGeneric(url, body,
                        method.getGenericReturnType(), method.getReturnType(), headerMap);
            } else if (RequestMethod.PUT.equals(httpMethod)) {
                if (args == null || args.length == 0) {
                    restTemplate.put(url, null);
                }
                Object body = null;
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof ServletRequest || args[i] instanceof ServletResponse) {
                        continue;
                    }
                    body = args[i];
                    break;
                }
                restTemplate.put(url, body);
            } else if (RequestMethod.DELETE.equals(httpMethod)) {
                restTemplate.delete(url, (Map<String, ?>) null);
            } else if (RequestMethod.PATCH.equals(httpMethod)) {
                if (args == null || args.length == 0) {
                    return restTemplate.patchForObjectGeneric(url, null, method.getReturnType(), headerMap);
                }
                Object body = null;
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof ServletRequest || args[i] instanceof ServletResponse) {
                        continue;
                    }
                    body = args[i];
                    break;
                }
                return restTemplate.patchForObjectGeneric(url, body, method.getReturnType(), headerMap);
            } else if (RequestMethod.OPTIONS.equals(httpMethod)) {
                throw new UnsupportedOperationException("HttpMethod options not support");
            } else if (RequestMethod.TRACE.equals(httpMethod)) {
                throw new UnsupportedOperationException("HttpMethod trace not support");
            }
        } finally {
            log.info("invoke-url: {}, cost {}ms", url, (System.currentTimeMillis() - st));
        }
        return null;
    }

    /**
     * 展开对象到url参数
     */
    private void expendObjectToUrlParams(Object object, Map<String, Object> params) throws IllegalAccessException {
        if (object == null || object.getClass().equals(Object.class)) {
            return;
        }
        final List<Field> fields = ReflectionUtils.getAllFields(object.getClass()).stream()
                .filter(p -> !Modifier.isStatic(p.getModifiers()))
                .filter(p -> {
                    ReflectionUtils.makeAccessible(p);
                    return p.isAccessible();
                }).collect(Collectors.toList());
        for (Field field : fields) {
            final Object val = field.get(object);
            if (val == null) {
                continue;
            }
            int type = 0;
            if (field.getType().isPrimitive() || val instanceof String || val instanceof Number) {
                type = 1;
            } else if (field.getType().isArray() || Collection.class.isAssignableFrom(field.getType())) {
                type = 2;
            }
            if (type == 0) {
                expendObjectToUrlParams(val, params);
            } else if (type == 1) {
                params.put(field.getName(), val);
            } else if (type == 2) {
                params.put(field.getName() + "[]", val);
            }
        }
    }

}
