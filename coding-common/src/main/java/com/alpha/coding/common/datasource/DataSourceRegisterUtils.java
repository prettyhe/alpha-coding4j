package com.alpha.coding.common.datasource;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.core.env.Environment;

import com.alibaba.druid.pool.DruidDataSource;
import com.alpha.coding.common.bean.spi.RegisterBeanDefinitionContext;

import lombok.extern.slf4j.Slf4j;

/**
 * DataSourceRegisterUtils
 *
 * @version 1.0
 * Date: 2020-03-21
 */
@Slf4j
public class DataSourceRegisterUtils {

    public static void register(RegisterBeanDefinitionContext context, String prefix) {
        final Environment env = context.getEnvironment();
        // 注册 读 DruidDataSource，beanName="#prefix + 'ReadDataSource'"
        BeanDefinitionBuilder readDefinitionBuilder =
                BeanDefinitionBuilder.genericBeanDefinition(DruidDataSource.class);
        buildDruidDataSourceBeanDefinitionBuilder(readDefinitionBuilder, env, prefix, "read");
        readDefinitionBuilder.addPropertyValue("url", env.getProperty(prefix + "." + "read.jdbc.url"));
        readDefinitionBuilder.addPropertyValue("username", env.getProperty(prefix + "." + "read.jdbc.username"));
        readDefinitionBuilder.addPropertyValue("password", env.getProperty(prefix + "." + "read.jdbc.password"));
        context.getRegistry().registerBeanDefinition(prefix + "ReadDataSource",
                readDefinitionBuilder.getBeanDefinition());
        log.info("register DruidDataSource: {}", prefix + "ReadDataSource");
        // 注册 写 DruidDataSource，beanName="#prefix + 'WriteDataSource'"
        BeanDefinitionBuilder writeDefinitionBuilder =
                BeanDefinitionBuilder.genericBeanDefinition(DruidDataSource.class);
        buildDruidDataSourceBeanDefinitionBuilder(writeDefinitionBuilder, env, prefix, "write");
        writeDefinitionBuilder.addPropertyValue("url", env.getProperty(prefix + "." + "write.jdbc.url"));
        writeDefinitionBuilder.addPropertyValue("username", env.getProperty(prefix + "." + "write.jdbc.username"));
        writeDefinitionBuilder.addPropertyValue("password", env.getProperty(prefix + "." + "write.jdbc.password"));
        context.getRegistry().registerBeanDefinition(prefix + "WriteDataSource",
                writeDefinitionBuilder.getBeanDefinition());
        log.info("register DruidDataSource: {}", prefix + "WriteDataSource");
    }

    private static void buildDruidDataSourceBeanDefinitionBuilder(BeanDefinitionBuilder builder,
                                                                  Environment environment,
                                                                  String prefix, String readWrite) {
        Function<String, List<String>> keysFunction = k -> Arrays.asList(
                prefix + "." + readWrite + "." + k,
                prefix + "." + k,
                k
        );
        builder.setInitMethodName("init").setDestroyMethodName("close");
        setIfAbsent(builder, environment, "driverClassName",
                keysFunction.apply("jdbc.driverClass"), null, null);
        setIfAbsent(builder, environment, "initialSize",
                keysFunction.apply("jdbc.initialSize"), Integer.class, 0);
        setIfAbsent(builder, environment, "minIdle",
                keysFunction.apply("jdbc.maxIdle"), Integer.class, 1);
        setIfAbsent(builder, environment, "maxActive",
                keysFunction.apply("jdbc.maxActive"), Integer.class, 1);
        setIfAbsent(builder, environment, "maxWait",
                keysFunction.apply("jdbc.maxWait"), Long.class, 60000L);
        setIfAbsent(builder, environment, "timeBetweenEvictionRunsMillis",
                keysFunction.apply("jdbc.timeBetweenEvictionRunsMillis"), Long.class, 60000L);
        setIfAbsent(builder, environment, "minEvictableIdleTimeMillis",
                keysFunction.apply("jdbc.minEvictableIdleTimeMillis"), Long.class, 300000L);
        setIfAbsent(builder, environment, "validationQuery",
                keysFunction.apply("jdbc.validationQuery"), null, "SELECT 'x' from dual");
        setIfAbsent(builder, environment, "testWhileIdle",
                keysFunction.apply("jdbc.testWhileIdle"), Boolean.class, true);
        setIfAbsent(builder, environment, "testOnBorrow",
                keysFunction.apply("jdbc.testOnBorrow"), Boolean.class, true);
        setIfAbsent(builder, environment, "testOnBorrow",
                keysFunction.apply("jdbc.testOnBorrow"), Boolean.class, true);
    }

    private static <T> BeanDefinitionBuilder setIfAbsent(BeanDefinitionBuilder builder, Environment environment,
                                                         String property, List<String> keys, Class<T> clz,
                                                         T defaultVal) {
        if (keys == null || keys.size() == 0) {
            if (defaultVal != null) {
                builder.addPropertyValue(property, defaultVal);
            }
            return builder;
        }
        // 使用存在的第一个
        final Optional<String> first = keys.stream().filter(k -> environment.containsProperty(k)).findFirst();
        first.ifPresent(k -> {
            if (clz != null && defaultVal != null) {
                builder.addPropertyValue(property, environment.getProperty(k, clz, defaultVal));
            } else if (clz != null) {
                builder.addPropertyValue(property, environment.getProperty(k, clz));
            } else {
                builder.addPropertyValue(property, environment.getProperty(k));
            }
        });
        if (!first.isPresent() && defaultVal != null) {
            builder.addPropertyValue(property, defaultVal);
        }
        return builder;
    }

}
