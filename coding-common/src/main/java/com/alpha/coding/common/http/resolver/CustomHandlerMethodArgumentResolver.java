package com.alpha.coding.common.http.resolver;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.alpha.coding.bo.annotation.HttpParam;
import com.alpha.coding.common.http.HttpParameterUtils;
import com.alpha.coding.common.http.HttpUtils;
import com.alpha.coding.common.http.model.HttpParamControl;

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
        final HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        final HttpParamControl httpParamControl = new HttpParamControl()
                .setMethod(parameter.getMethod()).setParameterIndex(parameter.getParameterIndex())
                .setParameterName(parameter.getParameterName()).setParameterType(parameter.getParameterType())
                .setGenericParameterType(parameter.getGenericParameterType())
                .setParameterAnnotations(parameter.getParameterAnnotations())
                .setParameterNameCandidates(new LinkedHashSet<>());
        final HttpParam annotation = parameter.getParameterAnnotation(HttpParam.class);
        httpParamControl.setRequired(annotation.required()).setDateFormatCandidate(annotation.dateFormatCandidate());
        httpParamControl.getParameterNameCandidates().addAll(Arrays.asList(annotation.name()));
        httpParamControl.getParameterNameCandidates().add(parameter.getParameterName());
        Object target = null;
        if (HttpUtils.isPrimitiveType(parameter.getParameterType())
                || Collection.class.isAssignableFrom(parameter.getParameterType())
                || Map.class.isAssignableFrom(parameter.getParameterType())
                || parameter.getParameterType().isArray()) {
            try {
                target = HttpUtils.parseParams(request, httpParamControl);
                if (target != null) {
                    return target;
                }
            } catch (Exception e) {
                if (log.isTraceEnabled()) {
                    log.debug("parseParams-fail, name: {}, names: {}, type: {}, msg: {}",
                            httpParamControl.getParameterName(), httpParamControl.getParameterNameCandidates(),
                            httpParamControl.getGenericParameterType().getTypeName(), e.getMessage(), e);
                }
            }
        } else {
            try {
                target = parameter.getParameterType().newInstance();
                HttpParameterUtils.parseHttpParameter(request, target);
            } catch (Exception e) {
                if (log.isTraceEnabled()) {
                    log.debug("parseHttpParameter-fail, name: {}, type: {}, msg: {}",
                            httpParamControl.getParameterName(), parameter.getParameterType().getTypeName(),
                            e.getMessage(), e);
                }
            }
        }
        return target;
    }

}
