package com.alpha.coding.bo.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.alpha.coding.bo.handler.XLSCellHandler;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface XLSLabel {

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
     * 作为excel头部使用的{@link XLSCellHandler}
     */
    Class<? extends XLSCellHandler> headCellHandler() default XLSCellHandler.class;

    /**
     * 作为excel头部使用的{@link XLSCellHandler}
     */
    Class<? extends XLSCellHandler> cellHandler() default XLSCellHandler.class;

    /**
     * 输出时间格式
     */
    String outDateFormat() default "yyyy-MM-dd HH:mm:ss";

}
