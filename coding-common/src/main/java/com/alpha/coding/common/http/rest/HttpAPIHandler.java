package com.alpha.coding.common.http.rest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.fastjson.JSON;
import com.alpha.coding.common.http.HttpUtils;

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

        RequestMapping[] ans = method.getAnnotationsByType(RequestMapping.class);
        if (ans == null || ans.length == 0) {
            throw new UnsupportedOperationException("method must have annotation RequestMapping");
        }
        String path = ans[0].value()[0];
        final RequestMethod[] methods = ans[0].method();
        RequestMethod httpMethod = (methods == null || methods.length == 0) ? RequestMethod.POST : methods[0];

        String url = selectPolicy.select(uriList) + path;
        final long st = System.currentTimeMillis();
        try {
            if (RequestMethod.GET.equals(httpMethod)) {
                if (args == null || args.length == 0) {
                    return restTemplate.getForObjectGeneric(url, method.getGenericReturnType(), method.getReturnType());
                }

                Map<Integer, Object> argsIndexMap = new HashMap<>();
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof ServletRequest || args[i] instanceof ServletResponse) {
                        continue;
                    }
                    argsIndexMap.put(i, args[i]);
                }
                if (argsIndexMap.size() == 0) {
                    return restTemplate.getForObjectGeneric(url, method.getGenericReturnType(), method.getReturnType());
                }
                Map<String, Object> map = new HashMap<>();
                argsIndexMap.keySet().stream().forEach(i -> {
                    final Object arg = argsIndexMap.get(i);
                    if (arg.getClass().isPrimitive() || arg instanceof String || arg instanceof Number) {
                        RequestParam an = method.getParameters()[i].getAnnotation(RequestParam.class);
                        if (an != null) {
                            map.put(an.value(), arg);
                        } else {
                            map.put(method.getParameters()[i].getName(), arg);
                        }
                    } else {
                        //  需要注意不能传基本类型之外的单类型
                        try {
                            map.putAll(convertToMapByJson(arg));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                url = HttpUtils.mergeUriWithParams(url, map);
                return restTemplate.getForObjectGeneric(url, method.getGenericReturnType(), method.getReturnType());
            } else if (RequestMethod.HEAD.equals(httpMethod)) {
                return restTemplate.headForHeaders(url, convertToMapByJson(args[0]));
            } else if (RequestMethod.POST.equals(httpMethod)) {
                return restTemplate.postForObjectGeneric(url, args[0],
                        method.getGenericReturnType(), method.getReturnType());
            } else if (RequestMethod.PUT.equals(httpMethod)) {
                restTemplate.put(url, args[0]);
            } else if (RequestMethod.DELETE.equals(httpMethod)) {
                restTemplate.delete(url, convertToMapByJson(args[0]));
            } else if (RequestMethod.PATCH.equals(httpMethod)) {
                return restTemplate.patchForObjectGeneric(url, args[0], method.getReturnType());
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

    private Map<String, ?> convertToMapByJson(Object object) {
        final String jsonString = JSON.toJSONString(object);
        return JSON.parseObject(jsonString);
    }

}
