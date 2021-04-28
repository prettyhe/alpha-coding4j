package com.alpha.coding.common.datasource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.core.env.Environment;

import com.alibaba.druid.pool.DruidDataSource;
import com.alpha.coding.common.bean.register.BeanDefineUtils;
import com.alpha.coding.common.bean.register.BeanDefinitionRegistryUtils;
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

    public static void register(RegisterBeanDefinitionContext context, CreateDataSourceEnv createDataSourceEnv) {
        final String prefix = createDataSourceEnv.getPrefix();
        final Environment env = context.getEnvironment();
        // 注册 读 DruidDataSource，beanName="#prefix + 'ReadDataSource'"
        BeanDefinitionBuilder readDefinitionBuilder =
                BeanDefinitionBuilder.genericBeanDefinition(DruidDataSource.class);
        buildDruidDataSourceBeanDefinitionBuilder(readDefinitionBuilder, env, createDataSourceEnv, "read");
        readDefinitionBuilder.addPropertyValue("url", env.getProperty(prefix + "." + "read.jdbc.url"));
        readDefinitionBuilder.addPropertyValue("username", env.getProperty(prefix + "." + "read.jdbc.username"));
        readDefinitionBuilder.addPropertyValue("password", env.getProperty(prefix + "." + "read.jdbc.password"));
        BeanDefinitionRegistryUtils.overideBeanDefinition(context.getRegistry(),
                prefix + "ReadDataSource",
                readDefinitionBuilder.getBeanDefinition());
        log.info("register DruidDataSource: {}", prefix + "ReadDataSource");
        // 注册 写 DruidDataSource，beanName="#prefix + 'WriteDataSource'"
        BeanDefinitionBuilder writeDefinitionBuilder =
                BeanDefinitionBuilder.genericBeanDefinition(DruidDataSource.class);
        buildDruidDataSourceBeanDefinitionBuilder(writeDefinitionBuilder, env, createDataSourceEnv, "write");
        writeDefinitionBuilder.addPropertyValue("url", env.getProperty(prefix + "." + "write.jdbc.url"));
        writeDefinitionBuilder.addPropertyValue("username", env.getProperty(prefix + "." + "write.jdbc.username"));
        writeDefinitionBuilder.addPropertyValue("password", env.getProperty(prefix + "." + "write.jdbc.password"));
        BeanDefinitionRegistryUtils.overideBeanDefinition(context.getRegistry(),
                prefix + "WriteDataSource",
                writeDefinitionBuilder.getBeanDefinition());
        log.info("register DruidDataSource: {}", prefix + "WriteDataSource");
    }

    private static void buildDruidDataSourceBeanDefinitionBuilder(BeanDefinitionBuilder builder,
                                                                  Environment environment,
                                                                  CreateDataSourceEnv createDataSourceEnv,
                                                                  String readWrite) {
        Function<String, List<String>> keysFunction = k -> Arrays.asList(
                createDataSourceEnv.getPrefix() + "." + readWrite + "." + k,
                createDataSourceEnv.getPrefix() + "." + k,
                k
        );
        final Map<String, Object> propertyMap = new HashMap<>();
        builder.setInitMethodName("init").setDestroyMethodName("close");
        BeanDefineUtils.setIfAbsent(builder, environment, "driverClassName",
                keysFunction.apply("jdbc.driverClass"), null, null, propertyMap);
        if ("com.ibm.db2.jcc.DB2Driver".equals(propertyMap.get("driverClassName"))) {
            createDataSourceEnv.setType("db2");
        }
        BeanDefineUtils.setIfAbsent(builder, environment, "initialSize",
                keysFunction.apply("jdbc.initialSize"), Integer.class, 0, propertyMap);
        BeanDefineUtils.setIfAbsent(builder, environment, "minIdle",
                keysFunction.apply("jdbc.minIdle"), Integer.class, 1, propertyMap);
        BeanDefineUtils.setIfAbsent(builder, environment, "maxActive",
                keysFunction.apply("jdbc.maxActive"), Integer.class, 1, propertyMap);
        BeanDefineUtils.setIfAbsent(builder, environment, "maxWait",
                keysFunction.apply("jdbc.maxWait"), Long.class, 60000L, propertyMap);
        BeanDefineUtils.setIfAbsent(builder, environment, "timeBetweenEvictionRunsMillis",
                keysFunction.apply("jdbc.timeBetweenEvictionRunsMillis"), Long.class, 60000L, propertyMap);
        BeanDefineUtils.setIfAbsent(builder, environment, "minEvictableIdleTimeMillis",
                keysFunction.apply("jdbc.minEvictableIdleTimeMillis"), Long.class, 300000L, propertyMap);
        // 检查连接的SQL，默认=>SELECT 'x' from dual, DB2默认=>SELECT 1 FROM SYSIBM.DUAL，可自定义覆盖
        BeanDefineUtils.setIfAbsent(builder, environment, "validationQuery",
                keysFunction.apply("jdbc.validationQuery"), null,
                !"db2".equals(createDataSourceEnv.getType()) ? "SELECT 'x' from dual" : "SELECT 1 FROM SYSIBM.DUAL",
                propertyMap);
        BeanDefineUtils.setIfAbsent(builder, environment, "testWhileIdle",
                keysFunction.apply("jdbc.testWhileIdle"), Boolean.class, true, propertyMap);
        BeanDefineUtils.setIfAbsent(builder, environment, "testOnBorrow",
                keysFunction.apply("jdbc.testOnBorrow"), Boolean.class, false, propertyMap);
        BeanDefineUtils.setIfAbsent(builder, environment, "testOnReturn",
                keysFunction.apply("jdbc.testOnReturn"), Boolean.class, false, propertyMap);
    }

}
