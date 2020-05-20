package com.alpha.coding.common.bean.fileinject.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.alpha.coding.common.bean.register.EnableAutoRegistrar;

/**
 * EnableFileInject
 *
 * @version 1.0
 * Date: 2020-03-18
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@EnableAutoRegistrar
@Repeatable(EnableFileInjects.class)
public @interface EnableFileInject {

    /**
     * 注入文件的路径
     */
    String[] basePath() default {};

    /**
     * 包含文件的过滤器
     */
    EnableFileInject.Filter[] includeFilters() default {};

    /**
     * 排除文件的过滤器
     */
    EnableFileInject.Filter[] excludeFilters() default {};

    @Retention(RetentionPolicy.RUNTIME)
    @Target({})
    public @interface Filter {

        String[] pattern() default {};
    }

}
