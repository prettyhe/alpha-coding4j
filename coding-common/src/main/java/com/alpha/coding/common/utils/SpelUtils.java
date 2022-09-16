package com.alpha.coding.common.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;

import com.alpha.coding.common.aop.assist.SpelExpressionParserFactory;

/**
 * SpelUtils
 *
 * @version 1.0
 * Date: 2022/8/24
 */
public class SpelUtils {

    private static final Map<String, Expression> EXPRESSION_CACHE = new ConcurrentHashMap<>(64);

    public static Expression parseExpression(String expr) {
        return EXPRESSION_CACHE.computeIfAbsent(expr,
                k -> SpelExpressionParserFactory.getDefaultParser().parseExpression(k));
    }

    public static Object getValue(String expr) {
        return parseExpression(expr).getValue();
    }

    public static Object getValue(Object rootObject, String expr) {
        return parseExpression(expr).getValue(rootObject);
    }

    public static <T> T getValue(Object rootObject, String expr, Class<T> clazz) {
        return parseExpression(expr).getValue(rootObject, clazz);
    }

    public static Object getValue(String expr, EvaluationContext evaluationContext) {
        return parseExpression(expr).getValue(evaluationContext);
    }

    public static Object getValue(Object rootObject, String expr, EvaluationContext evaluationContext) {
        return parseExpression(expr).getValue(evaluationContext, rootObject);
    }

    public static <T> T getValue(String expr, EvaluationContext evaluationContext, Class<T> clazz) {
        return parseExpression(expr).getValue(evaluationContext, clazz);
    }

    public static <T> T getValue(Object rootObject, String expr, EvaluationContext evaluationContext, Class<T> clazz) {
        return parseExpression(expr).getValue(evaluationContext, rootObject, clazz);
    }

}
