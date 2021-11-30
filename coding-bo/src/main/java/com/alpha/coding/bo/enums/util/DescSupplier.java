package com.alpha.coding.bo.enums.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.alpha.coding.bo.function.NullSupplier;

/**
 * DescSupplier
 *
 * @version 1.0
 * Date: 2021/11/29
 */
public interface DescSupplier {

    class Helper {
        private static final Map<Class, Method> GETTER_MAP = new HashMap<>();

        /**
         * 按方法名查找desc的getter方法，并包装成Supplier
         *
         * @param target               目标对象
         * @param descGetterMethodName getter方法名
         */
        public static Supplier<String> descSupply(Object target, String descGetterMethodName) {
            final Class clz = target.getClass();
            Method getter = null;
            try {
                if ((getter = GETTER_MAP.get(clz)) == null) {
                    if ((getter = clz.getDeclaredMethod(descGetterMethodName)) != null) {
                        if (getter.getReturnType().equals(String.class)) {
                            GETTER_MAP.put(clz, getter);
                        }
                    }
                }
            } catch (Exception e) {
                //
            }
            if (getter != null) {
                return () -> {
                    try {
                        return (String) GETTER_MAP.get(clz).invoke(target);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        return null;
                    }
                };
            }
            return NullSupplier.empty();
        }
    }

    default Supplier<String> descSupply() {
        return Helper.descSupply(this, "getDesc");
    }

}
