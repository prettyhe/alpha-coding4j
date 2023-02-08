package com.alpha.coding.common.utils.grovvy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.groovy.runtime.InvokerHelper;

import com.alpha.coding.common.utils.MD5Utils;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

/**
 * GroovyEngine
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class GroovyEngine {

    /**
     * 脚本缓存
     */
    private static final Map<String, Object> SCRIPT_CACHE = new ConcurrentHashMap<>();

    /**
     * 根据groovy代码执行脚本
     *
     * @param groovyCode groovy脚本
     * @param variables  变量
     * @return 执行结果
     */
    public static Object eval(String groovyCode, Map<String, ?> variables) {
        return eval(groovyCode, new Binding(variables));
    }

    /**
     * 根据groovy代码执行脚本
     *
     * @param groovyCode groovy脚本
     * @param variables  变量
     * @param initVars   初始化变量
     * @return 执行结果
     */
    public static Object eval(String groovyCode, Map<String, ?> variables, Map<String, ?> initVars) {
        Binding binding = new Binding(initVars == null ? variables : initVars);
        if (initVars != null && variables != null) {
            variables.forEach(binding::setVariable);
        }
        return eval(groovyCode, binding);
    }

    /**
     * 根据groovy代码执行脚本
     *
     * @param groovyCode groovy脚本
     * @param binding    变量
     * @return 执行结果
     */
    public static Object eval(String groovyCode, Binding binding) {
        final Script shell = (Script) SCRIPT_CACHE.computeIfAbsent(MD5Utils.md5(groovyCode),
                k -> new GroovyShell().parse(groovyCode));
        return InvokerHelper.createScript(shell.getClass(), binding).run();
    }

}
