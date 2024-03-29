package com.alpha.coding.common.datasource;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.alpha.coding.common.bean.register.EnableAutoRegistrar;

/**
 * EnableAutoDataSource 自动配置DataSource，使用Druid
 *
 * @version 1.0
 * Date: 2020-03-20
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@EnableAutoRegistrar
@Repeatable(EnableAutoDataSources.class)
public @interface EnableAutoDataSource {

    /**
     * 前缀，配置文件前缀
     */
    String prefix();

    /**
     * 类型，如mysql、db2，支持外部化配置，默认mysql
     */
    String type() default "${datasource.type:mysql}";

    /**
     * 数据源连接池类型
     */
    DataSourceConnectionPoolType connectionPoolType() default DataSourceConnectionPoolType.Auto;

}
