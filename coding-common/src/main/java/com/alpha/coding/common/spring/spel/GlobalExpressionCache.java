package com.alpha.coding.common.spring.spel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;

import com.alpha.coding.common.aop.assist.ExpressionKey;

/**
 * ExpressionCache
 *
 * @version 1.0
 * Date: 2023/1/31
 */
public class GlobalExpressionCache {

    private static final Map<ExpressionKey, Expression> EXPRESSION_CACHE = new ConcurrentHashMap<>(64);

    public static Map<ExpressionKey, Expression> getCache() {
        return EXPRESSION_CACHE;
    }

    /**
     * Return the {@link Expression} for the specified SpEL value
     * <p>Parse the expression if it hasn't been already.
     *
     * @param elementKey       the element on which the expression is defined
     * @param expression       the expression to parse
     * @param expressionParser the expressionParser
     */
    public static Expression getExpression(AnnotatedElementKey elementKey, String expression,
                                           ExpressionParser expressionParser) {
        ExpressionKey expressionKey = new ExpressionKey(elementKey, expression);
        Expression expr = EXPRESSION_CACHE.get(expressionKey);
        if (expr == null) {
            expr = expressionParser.parseExpression(expression);
            EXPRESSION_CACHE.put(expressionKey, expr);
        }
        return expr;
    }

}
