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
 * Date: 2020-02-21
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
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
     * 字段java类型
     */
    Class<?> javaType();

    /**
     * 作为excel头部使用的{@link CellHandler}
     */
    Class<? extends CellHandler> headCellHandler() default CellHandler.class;

    /**
     * 作为excel头部使用的{@link CellHandler}
     */
    Class<? extends CellHandler> cellHandler() default CellHandler.class;

}
