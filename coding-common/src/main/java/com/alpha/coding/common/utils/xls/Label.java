package com.alpha.coding.common.utils.xls;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Label
 *
 * @version 1.0
 * @see com.alpha.coding.bo.annotation.XLSLabel
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Deprecated
public @interface Label {

    /**
     * 字段说明
     */
    String memo() default "";

    /**
     * 在一行中的位置，从0开始
     */
    int order();

    /**
     * 输入输出转换时的中间类型，为void时不使用
     */
    Class<?> javaType() default void.class;

    /**
     * 作为excel头部cell使用的{@link CellHandler}
     */
    Class<? extends CellHandler>[] headCellHandler() default {};

    /**
     * 作为excel数据cell使用的{@link CellHandler}
     */
    Class<? extends CellHandler>[] cellHandler() default {};

    /**
     * 输出时间格式
     */
    String outDateFormat() default "yyyy-MM-dd HH:mm:ss";

}
