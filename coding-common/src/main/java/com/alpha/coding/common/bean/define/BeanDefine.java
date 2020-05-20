package com.alpha.coding.common.bean.define;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * BeanDefine
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface BeanDefine {

    /**
     * 定义类型
     */
    DefineType type() default DefineType.YAML;

    /**
     * bean名
     */
    String name() default "";

    /**
     * 源文件
     */
    String src() default "";

}
