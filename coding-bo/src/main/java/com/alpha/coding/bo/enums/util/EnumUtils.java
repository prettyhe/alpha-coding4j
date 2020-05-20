/**
 * Copyright
 */
package com.alpha.coding.bo.enums.util;

/**
 * EnumUtils
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class EnumUtils {

    public static <T extends Enum<T> & EnumWithCodeSupplier> T parse(Class<T> enumClass, Object code) {
        for (T t : enumClass.getEnumConstants()) {
            if ((code == null && t.codeSupply().get() == null) || (code != null && code.equals(t.codeSupply().get()))) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown code = " + code + " for enum class = " + enumClass);
    }

    public static <T extends Enum<T> & EnumWithCodeSupplier> T safeParse(Class<T> enumClass, Object code) {
        try {
            return parse(enumClass, code);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static <T extends Enum<T>> T safeParseByName(Class<T> enumClass, String name) {
        for (T t : enumClass.getEnumConstants()) {
            if (t.name().equals(name)) {
                return t;
            }
        }
        return null;
    }

    public static <T> T safeParseEnumByName(Class<T> enumClass, String name) {
        if (!enumClass.isEnum()) {
            return null;
        }
        for (T t : enumClass.getEnumConstants()) {
            if (((Enum) t).name().equals(name)) {
                return t;
            }
        }
        return null;
    }

}
