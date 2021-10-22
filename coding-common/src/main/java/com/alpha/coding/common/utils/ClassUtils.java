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

}
