/**
 * Copyright
 */
package com.alpha.coding.bo.enums.util;

import java.math.BigDecimal;
import java.util.Comparator;

/**
 * EnumUtils
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class EnumUtils {

    /**
     * 比较器，相等返回0，不等返回-1
     */
    public static final Comparator IGNORE_TYPE_COMPARATOR = (o1, o2) -> {
        if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 == null || o2 == null) {
            return -1;
        }
        if (o1.equals(o2)) {
            return 0;
        }
        try {
            BigDecimal bd1 = new BigDecimal(String.valueOf(o1).trim());
            BigDecimal bd2 = new BigDecimal(String.valueOf(o2).trim());
            if (bd1.compareTo(bd2) == 0) {
                return 0;
            }
        } catch (Exception e) {
            // 非数值
        }
        if (String.valueOf(o1).trim().equals(String.valueOf(o2).trim())) {
            return 0;
        }
        return -1;
    };

    public static <T extends Enum<T> & EnumWithCodeSupplier> T parse(Class<T> enumClass, Object code) {
        for (T t : enumClass.getEnumConstants()) {
            if ((code == null && t.codeSupply().get() == null) || (code != null && code.equals(t.codeSupply().get()))) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown code = " + code + " for enum class = " + enumClass);
    }

    public static <T extends Enum<T> & EnumWithCodeSupplier> T parse(Class<T> enumClass, Object code,
                                                                     Comparator comparator) {
        for (T t : enumClass.getEnumConstants()) {
            if (comparator.compare(code, t.codeSupply().get()) == 0) {
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

    public static <T extends Enum<T> & EnumWithCodeSupplier> T safeParse(Class<T> enumClass, Object code,
                                                                         Comparator comparator) {
        try {
            return parse(enumClass, code, comparator);
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

    public static <T extends Enum<T> & EnumWithCodeSupplier> T safeParseDefault(Class<T> enumClass, Object code) {
        return safeParse(enumClass, code, IGNORE_TYPE_COMPARATOR);
    }

}
