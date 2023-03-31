package com.alpha.coding.common.http.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.LinkedHashSet;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * HttpParamControl
 *
 * @version 1.0
 * Date: 2023/3/31
 */
@Data
@Accessors(chain = true)
public class HttpParamControl {

    private Method method;
    private int parameterIndex;
    private String parameterName;
    private LinkedHashSet<String> parameterNameCandidates;
    private Class<?> parameterType;
    private Type genericParameterType;
    private Annotation[] parameterAnnotations;
    private boolean required;
    private String[] dateFormatCandidate;

}
