package com.alpha.coding.common.utils;

import java.math.BigDecimal;

/**
 * MathUtils
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class MathUtils {

    public static final BigDecimal HUNDRED = new BigDecimal(100);
    public static final BigDecimal NEGATIVE_ONE = new BigDecimal(-1);

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
        return toBigDecimal(a).add(NEGATIVE_ONE.multiply(toBigDecimal(b)));
    }

    public static BigDecimal toBigDecimal(Number number) {
        return number == null ? null
                : (number instanceof BigDecimal ? (BigDecimal) number : new BigDecimal(String.valueOf(number)));
    }

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
        } else {
            throw new UnsupportedOperationException("not support for " + a.getClass());
        }
    }

    public static <T extends Number, A extends Number, B extends Number> T add(A a, B b, Class<T> clz) {
        if (a == null) {
            return convert(b, clz);
        }
        if (b == null) {
            return convert(a, clz);
        }
        return convert(new BigDecimal(String.valueOf(a)).add(new BigDecimal(String.valueOf(b))), clz);
    }

    public static <P extends Number, Q extends Number> P convert(Q q, Class<P> clz) {
        if (q == null) {
            return null;
        }
        BigDecimal bigDecimal = q instanceof BigDecimal ? (BigDecimal) q : new BigDecimal(String.valueOf(q));
        if (Integer.class.equals(clz)) {
            return (P) new Integer(bigDecimal.intValue());
        } else if (Long.class.equals(clz)) {
            return (P) new Long(bigDecimal.longValue());
        } else if (Short.class.equals(clz)) {
            return (P) new Short(bigDecimal.shortValue());
        } else if (Float.class.equals(clz)) {
            return (P) new Float(bigDecimal.floatValue());
        } else if (Double.class.equals(clz)) {
            return (P) new Double(bigDecimal.doubleValue());
        } else if (Byte.class.equals(clz)) {
            return (P) new Byte(bigDecimal.byteValue());
        } else if (BigDecimal.class.equals(clz)) {
            return (P) bigDecimal;
        } else {
            throw new UnsupportedOperationException("not support for " + clz);
        }
    }

    /**
     * 格式化成百分数:0.00255 -(precision=2)-> 0.26%
     *
     * @param bd        原始数据
     * @param precision 精度，百分后小数点位数
     */
    public static String percentFormat(BigDecimal bd, int precision) {
        if (bd == null) {
            return "";
        }
        BigDecimal divide = bd.multiply(HUNDRED).divide(BigDecimal.ONE, precision, BigDecimal.ROUND_HALF_UP);
        return divide.toString() + "%";
    }

    public static BigDecimal setScale(BigDecimal bd, int newScale) {
        return bd == null ? null : bd.setScale(newScale, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal setScale(BigDecimal bd, int newScale, int roundingMode) {
        return bd == null ? null : bd.setScale(newScale, roundingMode);
    }

}
