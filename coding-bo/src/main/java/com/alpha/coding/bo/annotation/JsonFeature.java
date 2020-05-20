package com.alpha.coding.bo.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Supplier;

/**
 * JsonFeature
 *
 * @version 1.0
 * Date: 2020-01-15
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
public @interface JsonFeature {

    /**
     * 时间格式
     */
    String timeFormat() default "yyyy-MM-dd HH:mm:ss";

    /**
     * 时间偏移函数
     */
    Class<? extends Supplier<Long>> timezoneOffsetSup() default NoneSupplier.class;

    /**
     * 无偏移函数
     */
    final class NoneSupplier implements Supplier<Long> {

        @Override
        public Long get() {
            return null;
        }
    }

}
