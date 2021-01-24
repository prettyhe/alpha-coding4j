package com.alpha.coding.bo.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.BigDecimal;

/**
 * JsonDecimal
 *
 * @version 1.0
 * Date: 2020-12-22
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
public @interface JsonDecimal {

    /**
     * 转成string类型
     */
    boolean asString() default false;

    /**
     * 转成string时后缀，如 %
     */
    String suffixForString() default "";

    /**
     * 倍数
     */
    String multiple() default "1";

    /**
     * 精度
     */
    int scale() default 0;

    /**
     * 舍入模式
     */
    int roundingMode() default BigDecimal.ROUND_HALF_UP;

    /**
     * 值为null时默认值
     */
    String defaultValueForNull() default "null";

}
