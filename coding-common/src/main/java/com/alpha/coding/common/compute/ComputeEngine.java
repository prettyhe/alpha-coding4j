package com.alpha.coding.common.compute;

import java.util.List;
import java.util.Stack;
import java.util.function.Function;

import com.alpha.coding.bo.base.Triple;
import com.alpha.coding.bo.base.Tuple;
import com.alpha.coding.common.utils.ListUtils;
import com.google.common.collect.Lists;

/**
 * ComputeEngine
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class ComputeEngine {

    /**
     * computeList 对List类型执行计算
     *
     * @param expression    表达式
     * @param keyFunction   集合元素取key的函数
     * @param valueFunction 表达式中参数取值的函数
     * @return java.util.List<T>
     */
    public static <T> List<T> computeList(String expression, Function<T, Object> keyFunction,
                                          Function<String, List<T>> valueFunction) {
        List<Tuple<String, Boolean>> postfixExp = toPostfix(parseToInfix(expression.toCharArray()));
        List<Triple<String, Boolean, List<T>>> triples = Lists.newArrayList();
        for (Tuple<String, Boolean> t : postfixExp) {
            if (t.getS()) {
                triples.add(new Triple<>(t.getF(), t.getS(), (List<T>) null));
            } else {
                triples.add(new Triple<>(t.getF(), t.getS(), valueFunction.apply(t.getF())));
            }
        }
        return processByPostfixExp(triples, keyFunction);
    }

    /**
     * processByPostfixExp 根据表达式元组执行计算
     *
     * @param triples     表达式元组
     * @param keyFunction 取key函数
     * @return java.util.List<T>
     */
    public static <T> List<T> processByPostfixExp(List<Triple<String, Boolean, List<T>>> triples,
                                                  final Function<T, Object> keyFunction) {
        Stack<List<T>> stack = new Stack<>();
        for (Triple<String, Boolean, List<T>> meta : triples) {
            if (!meta.getS()) {
                stack.push(meta.getT());
                continue;
            }
            final List<T> lb = stack.pop();
            final List<T> la = stack.pop();
            if (meta.getF().equals(ComputeConstant.Operator.add.getOp())) {
                stack.push(ListUtils.add(la, lb));
            } else if (meta.getF().equals(ComputeConstant.Operator.minus.getOp())) {
                stack.push(ListUtils.minus(la, lb, keyFunction));
            } else if (meta.getF().equals(ComputeConstant.Operator.of.getOp())) {
                stack.push(ListUtils.aOfB(la, lb, keyFunction));
            }
        }
        return stack.empty() ? null : stack.pop();
    }

    /**
     * 解析成元组(值，是否运算符)
     */
    public static List<Tuple<String, Boolean>> parseToInfix(char[] chars) {
        List<Tuple<String, Boolean>> infixExp = Lists.newArrayList();
        StringBuilder sb = new StringBuilder();
        for (char ch : chars) {
            if (isBlank(ch)) {
                continue;
            }
            if (isOp(ch)) {
                if (sb.length() > 0) {
                    infixExp.add(new Tuple<>(sb.toString(), false));
                    sb.delete(0, sb.length());
                }
                infixExp.add(new Tuple<>(new String(new char[] {ch}), true));
                continue;
            }
            if (isNumber(ch)) {
                sb.append(ch);
            }
        }
        if (sb.length() > 0) {
            infixExp.add(new Tuple<>(sb.toString(), false));
        }
        return infixExp;
    }

    /**
     * 中缀表达式变成后缀
     */
    public static List<Tuple<String, Boolean>> toPostfix(List<Tuple<String, Boolean>> tuples) {
        Stack<Tuple<String, Boolean>> stack = new Stack<>();
        List<Tuple<String, Boolean>> postfixExp = Lists.newArrayList();
        for (Tuple<String, Boolean> tuple : tuples) {
            if (!tuple.getS()) {
                postfixExp.add(tuple);
                continue;
            }
            if (stack.empty()) {
                stack.push(tuple);
                continue;
            }
            final char currOp = toChar(tuple.getF());
            if (isHigh(toChar(stack.peek().getF()), currOp)) {
                if (currOp != ')') {
                    do {
                        postfixExp.add(stack.pop());
                    } while (!stack.empty() && isHigh(toChar(stack.peek().getF()), currOp));
                    stack.push(tuple);
                } else {
                    while (!stack.empty() && toChar(stack.peek().getF()) != '(') {
                        postfixExp.add(stack.pop());
                    }
                    if (!stack.empty() && toChar(stack.peek().getF()) == '(') {
                        stack.pop();
                    }
                }
            } else {
                stack.push(tuple);
            }
        }
        while (!stack.empty()) {
            postfixExp.add(stack.pop());
        }
        return postfixExp;
    }

    private static char toChar(String string) {
        return string.toCharArray()[0];
    }

    private static boolean isOp(char ch) {
        return ch == '+' || ch == '-' || ch == '*' || ch == '|' || ch == '(' || ch == ')';
    }

    private static boolean isNumber(char ch) {
        return ch >= '0' && ch <= '9';
    }

    private static boolean isBlank(char ch) {
        return ch == ' ';
    }

    private static boolean isHigh(char topOp, char currOp) {
        if ((topOp == '+') && (currOp == '+')) {
            return true;
        }
        if ((topOp == '+') && (currOp == '-')) {
            return true;
        }
        if ((topOp == '-') && (currOp == '+')) {
            return true;
        }
        if ((topOp == '-') && (currOp == '-')) {
            return true;
        }
        if ((topOp == '*') && (currOp == '+')) {
            return true;
        }
        if ((topOp == '*') && (currOp == '-')) {
            return true;
        }
        if ((topOp == '*') && (currOp == '*')) {
            return true;
        }
        if ((topOp == '*') && (currOp == '|')) {
            return true;
        }
        if ((topOp == '|') && (currOp == '+')) {
            return true;
        }
        if ((topOp == '|') && (currOp == '-')) {
            return true;
        }
        if ((topOp == '|') && (currOp == '*')) {
            return true;
        }
        if ((topOp == '|') && (currOp == '|')) {
            return true;
        }
        if (currOp == ')') {
            return true;
        }
        return false;
    }

}
