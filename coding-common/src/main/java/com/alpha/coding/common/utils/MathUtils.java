package com.alpha.coding.common.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import lombok.extern.slf4j.Slf4j;

/**
 * MathUtils
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class MathUtils {

    public static final BigDecimal HUNDRED = new BigDecimal(100);
    public static final BigDecimal NEGATIVE_ONE = new BigDecimal(-1);
    public static final Set<Class<?>> PRIMITIVE_NUMBER_TYPES = new HashSet<>(Arrays.asList(int.class, long.class,
            short.class, byte.class, float.class, double.class));

    public static BigDecimal multiply(Number a, Number b) {
        if (a == null || b == null) {
            return null;
        }
        return toBigDecimal(a).multiply(toBigDecimal(b));
    }

    public static Double divide(Number a, Number b) {
        return (a == null || b == null || !(b.doubleValue() > 0 || b.doubleValue() < 0)) ? null
                : (a.doubleValue() / b.doubleValue());
    }

    public static BigDecimal divide(Number a, Number b, int scale, int roundingMode) {
        if (a == null || b == null || !(b.doubleValue() > 0 || b.doubleValue() < 0)) {
            return null;
        }
        return toBigDecimal(a).divide(toBigDecimal(b), scale, roundingMode);
    }

    public static BigDecimal minus(Number a, Number b) {
        if (a == null || b == null) {
            return null;
        }
        return toBigDecimal(a).subtract(toBigDecimal(b));
    }

    public static BigDecimal toBigDecimal(Number number) {
        return number == null ? null
                : (number instanceof BigDecimal ? (BigDecimal) number : new BigDecimal(String.valueOf(number)));
    }

    /**
     * 两数(相同类型)相加
     *
     * @param a 数值a
     * @param b 数值b
     * @return 和
     * @throws UnsupportedOperationException 不支持的数值类型
     */
    public static <T extends Number> T add(T a, T b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        if (a instanceof Integer) {
            return (T) new Integer(a.intValue() + b.intValue());
        } else if (a instanceof Long) {
            return (T) new Long(a.longValue() + b.longValue());
        } else if (a instanceof Short) {
            return (T) new Short((short) (a.shortValue() + b.shortValue()));
        } else if (a instanceof Float) {
            return (T) new Float(a.floatValue() + b.floatValue());
        } else if (a instanceof Double) {
            return (T) new Double(a.doubleValue() + b.doubleValue());
        } else if (a instanceof Byte) {
            return (T) new Byte((byte) (a.byteValue() + b.byteValue()));
        } else if (a instanceof BigDecimal) {
            return (T) ((BigDecimal) a).add((BigDecimal) b);
        } else if (a instanceof BigInteger) {
            return (T) ((BigInteger) a).add((BigInteger) convert(b, BigInteger.class));
        } else if (a instanceof AtomicInteger) {
            return (T) new AtomicInteger(a.intValue() + b.intValue());
        } else if (a instanceof AtomicLong) {
            return (T) new AtomicLong(a.longValue() + b.longValue());
        } else {
            throw new UnsupportedOperationException("not support for " + a.getClass().getName());
        }
    }

    /**
     * 两数相加，并转为目标数值类型
     *
     * @param <T> 目标数值类型
     * @param <A> 数值A类型
     * @param <B> 数值B类型
     * @param a   数值a
     * @param b   数值b
     * @param clz 目标数值类型
     * @return 目标数值
     * @throws UnsupportedOperationException 不支持的类型
     */
    public static <T extends Number, A extends Number, B extends Number> T add(A a, B b, Class<T> clz) {
        if (a == null) {
            return (T) convert(b, clz);
        }
        if (b == null) {
            return (T) convert(a, clz);
        }
        return (T) convert(new BigDecimal(String.valueOf(a)).add(new BigDecimal(String.valueOf(b))), clz);
    }

    /**
     * 数值转换
     *
     * @param <T>    数值类型
     * @param number 数值
     * @param type   新数值类型
     * @return 新数值类型的数值
     * @throws UnsupportedOperationException 不支持的类型
     */
    public static <T extends Number> Number convert(T number, Type type) {
        if (number == null) {
            return null;
        }
        if (type == byte.class || type == Byte.class) {
            return number.byteValue();
        } else if (type == short.class || type == Short.class) {
            return number.shortValue();
        } else if (type == int.class || type == Integer.class) {
            return number.intValue();
        } else if (type == long.class || type == Long.class) {
            return number.longValue();
        } else if (type == float.class || type == Float.class) {
            return number.floatValue();
        } else if (type == double.class || type == Double.class) {
            return number.doubleValue();
        } else if (type == BigInteger.class) {
            return new BigInteger(String.valueOf(number));
        } else if (type == BigDecimal.class) {
            return number instanceof BigDecimal ? (BigDecimal) number : new BigDecimal(String.valueOf(number));
        } else if (type == AtomicInteger.class) {
            return new AtomicInteger(number.intValue());
        } else if (type == AtomicLong.class) {
            return new AtomicLong(number.longValue());
        } else {
            try {
                if (!Number.class.isAssignableFrom(Class.forName(type.getTypeName()))) {
                    throw new UnsupportedOperationException("not support for " + type.getTypeName());
                }
            } catch (ClassNotFoundException e) {
                throw new UnsupportedOperationException("not support for " + type.getTypeName());
            }
            return number;
        }
    }

    /**
     * 转换为数值
     *
     * @param val  数值或数值字符串
     * @param type 类型
     * @return 目标类型的数值
     * @throws UnsupportedOperationException 不支持的类型
     */
    public static Number convertToNumber(Object val, Type type) {
        if (val == null) {
            return null;
        }
        if (val instanceof Number) {
            return convert((Number) val, type);
        }
        return convert(new BigDecimal(String.valueOf(val)), type);
    }

    /**
     * 格式化成百分数:0.00255 -(precision=2)-> 0.26%
     *
     * @param bd        原始数据
     * @param precision 精度，百分后小数点位数
     */
    public static String percentFormat(BigDecimal bd, int precision) {
        return percentFormatWithDefault(bd, precision, "");
    }

    /**
     * 格式化成百分数:0.00255 -(precision=2)-> 0.26%
     *
     * @param bd         原始数据
     * @param precision  精度，百分后小数点位数
     * @param defaultVal 默认值
     */
    public static String percentFormatWithDefault(BigDecimal bd, int precision, String defaultVal) {
        if (bd == null) {
            return defaultVal;
        }
        BigDecimal divide = bd.multiply(HUNDRED).divide(BigDecimal.ONE, precision, RoundingMode.HALF_UP);
        return divide.toPlainString() + "%";
    }

    public static BigDecimal setScale(BigDecimal bd, int newScale) {
        return bd == null ? null : bd.setScale(newScale, RoundingMode.HALF_UP);
    }

    public static BigDecimal setScale(BigDecimal bd, int newScale, int roundingMode) {
        return bd == null ? null : bd.setScale(newScale, roundingMode);
    }

    /**
     * 初始化对象的数值属性为0
     *
     * @param target        对象
     * @param excludeFields 排除的字段
     */
    public static void initNumberPropertyAsZero(Object target, String... excludeFields) {
        if (target == null) {
            return;
        }
        Class<?> superClz = target.getClass();
        while (!superClz.equals(Object.class)) {
            for (Field field : superClz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                if (Arrays.stream(excludeFields).anyMatch(p -> p.equals(field.getName()))) {
                    continue;
                }
                final Class<?> type = field.getType();
                if (!Number.class.isAssignableFrom(type)) {
                    continue;
                }
                try {
                    field.setAccessible(true);
                    if (field.get(target) == null) {
                        field.set(target, convert(0, type));
                    }
                } catch (Exception e) {
                    log.warn("init number fail, name={},type={},ex={},msg={}",
                            field.getName(), type.getName(), e.getClass().getName(), e.getMessage());
                }
            }
            superClz = superClz.getSuperclass();
        }
    }

}
