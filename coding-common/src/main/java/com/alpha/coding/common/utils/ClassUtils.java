package com.alpha.coding.common.utils;

/**
 * ClassUtils
 *
 * @version 1.0
 * Date: 2020/4/4
 */
public class ClassUtils {

    public static Class loadClass(String className, boolean initialize) throws ClassNotFoundException {
        try {
            return Class.forName(className, initialize, ClassUtils.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            try {
                return Class.forName(className, initialize, classLoader);
            } catch (ClassNotFoundException e1) {
                throw e1;
            }
        }
    }

}
