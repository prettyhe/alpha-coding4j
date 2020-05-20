package com.alpha.coding.common.http.resolver;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.alibaba.fastjson.JSON;
import com.alpha.coding.bo.annotation.HttpParam;
import com.alpha.coding.common.http.HttpParameterUtils;
import com.alpha.coding.common.http.HttpUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * CustomHandlerMethodArgumentResolver
 *
 * @version 1.0
 * Date: 2019-12-27
 */
@Slf4j
public class CustomHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(HttpParam.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        final Class<?> type = parameter.getParameterType();
        final HttpParam annotation = parameter.getParameterAnnotation(HttpParam.class);
        Object target = null;
        if (annotation.name() != null) {
            for (String name : annotation.name()) {
                target = parseFromParameter(request, name, type);
                if (target != null) {
                    return target;
                }
            }
        } else {
            target = parseFromParameter(request, parameter.getParameterName(), type);
            if (target != null) {
                return target;
            }
        }
        try {
            target = HttpUtils.parseParams(request, type);
            if (target != null) {
                return target;
            }
        } catch (Exception e) {
        }
        if (!type.isPrimitive()) {
            try {
                target = type.newInstance();
                HttpParameterUtils.parseHttpParameter(request, target);
            } catch (Exception e) {
            }
        }
        return target;
    }

    private Object parseFromParameter(HttpServletRequest request, String name, Class<?> type) {
        try {
            return JSON.parseObject(request.getParameter(name), type);
        } catch (Exception e) {
            return null;
        }
    }
}
