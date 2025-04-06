package com.alpha.coding.common.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

/**
 * ClassUtils
 *
 * @version 1.0
 * Date: 2020/4/4
 */
public class ClassUtils {

    /**
     * 代理 class 的名称
     */
    private static final List<String> PROXY_CLASS_NAMES = Arrays.asList(
            "net.sf.cglib.proxy.Factory",
            // cglib
            "org.springframework.cglib.proxy.Factory",
            "javassist.util.proxy.ProxyObject",
            // javassist
            "org.apache.ibatis.javassist.util.proxy.ProxyObject");

    public static Class<?> loadClass(String className, boolean initialize) throws ClassNotFoundException {
        try {
            return Class.forName(className, initialize, ClassUtils.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            return Class.forName(className, initialize, classLoader);
        }
    }

    public static String getCallerCallerClassName() {
        try {
            StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
            String callerClassName = null;
            for (int i = 1; i < stElements.length; i++) {
                StackTraceElement ste = stElements[i];
                if (!ste.getClassName().equals(ClassUtils.class.getName())
                        && ste.getClassName().indexOf("java.lang.Thread") != 0) {
                    if (callerClassName == null) {
                        callerClassName = ste.getClassName();
                    } else if (!callerClassName.equals(ste.getClassName())) {
                        return ste.getClassName() + "." + ste.getMethodName();
                    }
                }
            }
            return null;
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * <p>
     * 根据指定的 class ， 实例化一个对象，根据构造参数来实例化
     * </p>
     * <p>
     * 在 java9 及其之后的版本 Class.newInstance() 方法已被废弃
     * </p>
     *
     * @param clazz 需要实例化的对象
     * @param <T>   类型，由输入类型决定
     * @return 返回新的实例
     */
    public static <T> T newInstance(Class<T> clazz)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<T> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    /**
     * 判断是否为代理对象
     *
     * @param clazz 传入 class 对象
     * @return 如果对象class是代理 class，返回 true
     */
    public static boolean isProxy(Class<?> clazz) {
        if (clazz != null) {
            for (Class<?> cls : clazz.getInterfaces()) {
                if (PROXY_CLASS_NAMES.contains(cls.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * <p>
     * 获取当前对象的 class
     * </p>
     *
     * @param clazz 传入
     * @return 如果是代理的class，返回父 class，否则返回自身
     */
    public static Class<?> getUserClass(Class<?> clazz) {
        return isProxy(clazz) ? clazz.getSuperclass() : clazz;
    }

}

