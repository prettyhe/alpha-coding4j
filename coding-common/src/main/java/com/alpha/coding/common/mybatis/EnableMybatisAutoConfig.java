package com.alpha.coding.common.mybatis;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.alpha.coding.common.bean.register.EnableAutoRegistrar;
import com.alpha.coding.common.datasource.EnableAutoDataSource;

/**
 * EnableMybatisAutoConfig
 *
 * @version 1.0
 * Date: 2020-03-21
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@EnableAutoRegistrar
@Import(AutoMybatisConfiguration.class)
@Repeatable(EnableMybatisAutoConfigs.class)
public @interface EnableMybatisAutoConfig {

    /**
     * DataSource 配置
     */
    EnableAutoDataSource dataSource();

    /**
     * 适用于同一个数据源多份mybatis映射配置场景，使用此字段区分
     */
    String tag() default "";

    /**
     * beanName of org.apache.ibatis.session.Configuration
     */
    String configuration() default "";

    /**
     * configLocation
     */
    String configLocation() default "";

    /**
     * xml文件路径
     */
    String[] mapperLocations() default {};

    /**
     * model包路径
     */
    String typeAliasesPackage() default "";

    /**
     * mapper
     */
    String mapperBasePackage();

    /**
     * 为true需要引入：com.github.miemiedev:mybatis-paginator
     */
    boolean enablePageHandlerInterceptor() default false;

    /**
     * 启用：com.alpha.coding.common.mybatis.ShowSqlInterceptor
     */
    boolean enableShowSqlInterceptor() default true;

    /**
     * 启用com.alpha.coding.common.mybatis.ShowSqlInterceptor时的配置参数,k=v形式，v支持外部配置化参数
     * <p>
     * sqlIdAbbreviated=true
     * </p>
     */
    String[] showSqlInterceptorProperties() default {
            "sqlIdAbbreviated=${mybatis.plugins.ShowSqlInterceptor.sqlIdAbbreviated:true}",
            "enableShowDatabaseName=${mybatis.plugins.ShowSqlInterceptor.enableShowDatabaseName:false}"};

    /**
     * 其它扩展插件(org.apache.ibatis.plugin.Interceptor)beanName
     */
    String[] extPlugins() default {};

    /**
     * 强制使用(写数据源)的sqlId正则
     */
    String[] forceUseWriteDataSourceSql() default {};

}
