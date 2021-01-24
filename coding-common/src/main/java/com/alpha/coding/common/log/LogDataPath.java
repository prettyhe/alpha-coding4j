package com.alpha.coding.common.log;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * LogDataPath
 *
 * @version 1.0
 * Date: 2021-01-21
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface LogDataPath {

    /**
     * 请求参数中忽略打印的字段的JsonPath，
     * 以参数下标(从0开始，*表示所有参数)开始，如0.$['name']，即从第一个点号之后的为该参数的真实JsonPath
     *
     * @see com.jayway.jsonpath.JsonPath
     * @see <a href="https://github.com/json-path/JsonPath">https://github.com/json-path/JsonPath</a>
     */
    String[] reqIgnoreFieldPath() default {};

    /**
     * 响应参数中忽略打印的字段的JsonPath
     *
     * @see com.jayway.jsonpath.JsonPath
     * @see <a href="https://github.com/json-path/JsonPath">https://github.com/json-path/JsonPath</a>
     */
    String[] resIgnoreFieldPath() default {};

    /**
     * 请求参数中保留打印的字段的JsonPath，
     * 以参数下标(从0开始，*表示所有参数)开始，如0.$['name']，即从第一个点号之后的为该参数的真实JsonPath
     *
     * @see com.jayway.jsonpath.JsonPath
     * @see <a href="https://github.com/json-path/JsonPath">https://github.com/json-path/JsonPath</a>
     */
    String[] reqRetainFieldPath() default {};

    /**
     * 响应参数中保留打印的字段的JsonPath
     *
     * @see com.jayway.jsonpath.JsonPath
     * @see <a href="https://github.com/json-path/JsonPath">https://github.com/json-path/JsonPath</a>
     */
    String[] resRetainFieldPath() default {};

}
