package com.alpha.coding.common.datasource;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.core.env.Environment;
import org.springframework.util.ClassUtils;

import com.alpha.coding.bo.function.common.Converter;
import com.alpha.coding.common.bean.register.BeanDefineUtils;
import com.alpha.coding.common.bean.register.BeanDefinitionRegistryUtils;
import com.alpha.coding.common.bean.spi.RegisterBeanDefinitionContext;
import com.alpha.coding.common.utils.StringUtils;

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
        final DataSourceConnectionPoolType connectionPoolType = createDataSourceEnv.getConnectionPoolType();
        if (connectionPoolType == null || connectionPoolType == DataSourceConnectionPoolType.Auto) {
            if (ClassUtils.isPresent(DataSourceConnectionPoolType.Druid.getDataSourceClass(),
                    ClassUtils.getDefaultClassLoader())) {
                registerDruid(context, createDataSourceEnv);
            } else if (ClassUtils.isPresent(DataSourceConnectionPoolType.C3P0.getDataSourceClass(),
                    ClassUtils.getDefaultClassLoader())) {
                registerC3P0(context, createDataSourceEnv);
            } else if (ClassUtils.isPresent(DataSourceConnectionPoolType.HikariCP.getDataSourceClass(),
                    ClassUtils.getDefaultClassLoader())) {
                registerHikariCP(context, createDataSourceEnv);
            } else {
                throw new IllegalArgumentException("None supported DataSource class!");
            }
        } else if (DataSourceConnectionPoolType.Druid == connectionPoolType) {
            registerDruid(context, createDataSourceEnv);
        } else if (DataSourceConnectionPoolType.C3P0 == connectionPoolType) {
            registerC3P0(context, createDataSourceEnv);
        } else if (DataSourceConnectionPoolType.HikariCP == connectionPoolType) {
            registerHikariCP(context, createDataSourceEnv);
        } else {
            throw new IllegalArgumentException("unsupported connectionPoolType: " + connectionPoolType);
        }
    }

    /**
     * 注册 DruidDataSource
     */
    private static void registerDruid(RegisterBeanDefinitionContext context, CreateDataSourceEnv createDataSourceEnv) {
        final String prefix = createDataSourceEnv.getPrefix();
        final Environment env = context.getEnvironment();
        // 注册 读 DruidDataSource，beanName="#prefix + 'ReadDataSource'"
        BeanDefinitionBuilder readDefinitionBuilder =
                BeanDefinitionBuilder.genericBeanDefinition(DataSourceConnectionPoolType.Druid.getDataSourceClass());
        completeDruidDataSourceBeanDefinition(readDefinitionBuilder, env, createDataSourceEnv, "read");
        readDefinitionBuilder.addPropertyValue("url", BeanDefineUtils.fetchProperty(env,
                Arrays.asList(prefix + "." + "read.jdbc.url", prefix + ".jdbc.url", "read.jdbc.url"),
                StringUtils::isNotBlank, String.class, null));
        readDefinitionBuilder.addPropertyValue("username", BeanDefineUtils.fetchProperty(env,
                Arrays.asList(prefix + "." + "read.jdbc.username", prefix + ".jdbc.username", "read.jdbc.username"),
                StringUtils::isNotBlank, String.class, null));
        readDefinitionBuilder.addPropertyValue("password", BeanDefineUtils.fetchProperty(env, Arrays.asList(
                prefix + "." + "read.jdbc.password", prefix + ".jdbc.password", "read.jdbc.password"),
                StringUtils::isNotBlank, String.class, null));
        if (BeanDefinitionRegistryUtils.overideBeanDefinition(context.getRegistry(),
                prefix + "ReadDataSource", readDefinitionBuilder.getBeanDefinition(), true)) {
            log.info("register DruidDataSource: {}", prefix + "ReadDataSource");
        }
        // 注册 写 DruidDataSource，beanName="#prefix + 'WriteDataSource'"
        BeanDefinitionBuilder writeDefinitionBuilder =
                BeanDefinitionBuilder.genericBeanDefinition(DataSourceConnectionPoolType.Druid.getDataSourceClass());
        completeDruidDataSourceBeanDefinition(writeDefinitionBuilder, env, createDataSourceEnv, "write");
        writeDefinitionBuilder.addPropertyValue("url", BeanDefineUtils.fetchProperty(env,
                Arrays.asList(prefix + "." + "write.jdbc.url", prefix + ".jdbc.url", "write.jdbc.url"),
                StringUtils::isNotBlank, String.class, null));
        writeDefinitionBuilder.addPropertyValue("username", BeanDefineUtils.fetchProperty(env,
                Arrays.asList(prefix + "." + "write.jdbc.username", prefix + ".jdbc.username", "write.jdbc.username"),
                StringUtils::isNotBlank, String.class, null));
        writeDefinitionBuilder.addPropertyValue("password", BeanDefineUtils.fetchProperty(env, Arrays.asList(
                prefix + "." + "write.jdbc.password", prefix + ".jdbc.password", "write.jdbc.password"),
                StringUtils::isNotBlank, String.class, null));
        if (BeanDefinitionRegistryUtils.overideBeanDefinition(context.getRegistry(),
                prefix + "WriteDataSource", writeDefinitionBuilder.getBeanDefinition(), true)) {
            log.info("register DruidDataSource: {}", prefix + "WriteDataSource");
        }
    }

    /**
     * 完善 DruidDataSource
     */
    private static void completeDruidDataSourceBeanDefinition(BeanDefinitionBuilder builder,
                                                              Environment environment,
                                                              CreateDataSourceEnv createDataSourceEnv,
                                                              String readWrite) {
        final Function<String, List<String>> keysFunction = k -> Arrays.asList(
                createDataSourceEnv.getPrefix() + "." + readWrite + "." + k,
                createDataSourceEnv.getPrefix() + "." + k,
                readWrite + "." + k,
                k
        );
        final Map<String, Object> propertyMap = new HashMap<>();
        builder.setInitMethodName("init").setDestroyMethodName("close");
        BeanDefineUtils.setIfAbsent(builder, environment, "driverClassName",
                keysFunction.apply("jdbc.driverClass"), null, null, propertyMap);
        if ("com.ibm.db2.jcc.DB2Driver".equals(propertyMap.get("driverClassName"))) {
            createDataSourceEnv.setType("db2");
        }
        // 初始化时建立物理连接的个数
        BeanDefineUtils.setIfAbsent(builder, environment, "initialSize",
                keysFunction.apply("jdbc.initialSize"), Integer.class, 0, propertyMap);
        // 最小连接池数量
        BeanDefineUtils.setIfAbsent(builder, environment, "minIdle",
                keysFunction.apply("jdbc.minIdle"), Integer.class, 1, propertyMap);
        // 最大连接池数量
        BeanDefineUtils.setIfAbsent(builder, environment, "maxActive",
                keysFunction.apply("jdbc.maxActive"), Integer.class, 5, propertyMap);
        // 获取连接时最大等待时间，单位毫秒
        BeanDefineUtils.setIfAbsent(builder, environment, "maxWait",
                keysFunction.apply("jdbc.maxWait"), Long.class, 60000L, propertyMap);
        // 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位毫秒
        BeanDefineUtils.setIfAbsent(builder, environment, "timeBetweenEvictionRunsMillis",
                keysFunction.apply("jdbc.timeBetweenEvictionRunsMillis"), Long.class, 60000L, propertyMap);
        // 连接保持空闲而不被驱逐的最小时间，单位毫秒
        BeanDefineUtils.setIfAbsent(builder, environment, "minEvictableIdleTimeMillis",
                keysFunction.apply("jdbc.minEvictableIdleTimeMillis"), Long.class, 300000L, propertyMap);
        // 连接保持空闲而不被驱逐的最大时间，单位毫秒
        BeanDefineUtils.setIfAbsent(builder, environment, "maxEvictableIdleTimeMillis",
                keysFunction.apply("jdbc.maxEvictableIdleTimeMillis"), Long.class, null, propertyMap);
        // 检查连接的SQL，默认=>SELECT 1, DB2默认=>SELECT 1 FROM SYSIBM.DUAL，可自定义覆盖
        BeanDefineUtils.setIfAbsent(builder, environment, "validationQuery",
                keysFunction.apply("jdbc.validationQuery"), null,
                !"db2".equals(createDataSourceEnv.getType()) ? "SELECT 1" : "SELECT 1 FROM SYSIBM.DUAL",
                propertyMap);
        // 检查连接是否有效的超时时间，单位秒
        BeanDefineUtils.setIfAbsent(builder, environment, "validationQueryTimeout",
                keysFunction.apply("jdbc.validationQueryTimeout"), Long.class, null, propertyMap);
        // 申请连接的时候检测，如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效
        BeanDefineUtils.setIfAbsent(builder, environment, "testWhileIdle",
                keysFunction.apply("jdbc.testWhileIdle"), Boolean.class, true, propertyMap);
        // 申请连接时执行validationQuery检测连接是否有效
        BeanDefineUtils.setIfAbsent(builder, environment, "testOnBorrow",
                keysFunction.apply("jdbc.testOnBorrow"), Boolean.class, false, propertyMap);
        // 归还连接时执行validationQuery检测连接是否有效
        BeanDefineUtils.setIfAbsent(builder, environment, "testOnReturn",
                keysFunction.apply("jdbc.testOnReturn"), Boolean.class, false, propertyMap);
        // 是否缓存preparedStatement，也就是PSCache
        BeanDefineUtils.setIfAbsent(builder, environment, "poolPreparedStatements",
                keysFunction.apply("jdbc.poolPreparedStatements"), Boolean.class, null, propertyMap);
        // 要启用PSCache，必须配置大于0，当大于0时，poolPreparedStatements自动触发修改为true
        BeanDefineUtils.setIfAbsent(builder, environment, "maxOpenPreparedStatements",
                keysFunction.apply("jdbc.maxOpenPreparedStatements"), Integer.class, null, propertyMap);
        // 要启用PSCache，必须配置大于0，当大于0时，poolPreparedStatements自动触发修改为true
        BeanDefineUtils.setIfAbsent(builder, environment, "maxPoolPreparedStatementPerConnectionSize",
                keysFunction.apply("jdbc.maxPoolPreparedStatementPerConnectionSize"), Integer.class, null, propertyMap);
        // 连接池内的minIdle数量以内的连接，如果连接空闲时间超过minEvictableIdleTimeMillis，则执行keepAlive检测
        BeanDefineUtils.setIfAbsent(builder, environment, "keepAlive",
                keysFunction.apply("jdbc.keepAlive"), Boolean.class, null, propertyMap);
        // 当连接的空闲时间大于keepAliveBetweenTimeMillis（默认2分钟），但是小于minEvictableIdleTimeMillis（默认30分钟），
        // Druid会通过调用validationQuery保持该连接的有效性
        // 当连接空闲时间大于minEvictableIdleTimeMillis，单位毫秒，Druid会直接将该连接关闭，keepAlive会无效
        BeanDefineUtils.setIfAbsent(builder, environment, "keepAliveBetweenTimeMillis",
                keysFunction.apply("jdbc.keepAliveBetweenTimeMillis"), Long.class, null, propertyMap);
        // 配置监控统计拦截的filters，去掉后监控界面sql无法统计
        BeanDefineUtils.setIfAbsent(builder, environment, "filters",
                keysFunction.apply("jdbc.filters"), String.class, null, propertyMap);
        // 通过connectProperties属性来打开mergeSql功能
        // 如慢SQL记录可配置:druid.stat.mergeSql=true;druid.stat.slowSqlMillis=500
        BeanDefineUtils.setIfAbsent(builder, environment, "connectionProperties",
                keysFunction.apply("jdbc.connectionProperties"), String.class, null, propertyMap);
    }

    /**
     * 注册 ComboPooledDataSource
     */
    private static void registerC3P0(RegisterBeanDefinitionContext context, CreateDataSourceEnv createDataSourceEnv) {
        final String prefix = createDataSourceEnv.getPrefix();
        final Environment env = context.getEnvironment();
        // 注册 读 DruidDataSource，beanName="#prefix + 'ReadDataSource'"
        BeanDefinitionBuilder readDefinitionBuilder =
                BeanDefinitionBuilder.genericBeanDefinition(DataSourceConnectionPoolType.C3P0.getDataSourceClass());
        completeC3P0DataSourceBeanDefinition(readDefinitionBuilder, env, createDataSourceEnv, "read");
        readDefinitionBuilder.addPropertyValue("jdbcUrl", BeanDefineUtils.fetchProperty(env,
                Arrays.asList(prefix + "." + "read.jdbc.url", prefix + ".jdbc.url", "read.jdbc.url"),
                StringUtils::isNotBlank, String.class, null));
        readDefinitionBuilder.addPropertyValue("user", BeanDefineUtils.fetchProperty(env,
                Arrays.asList(prefix + "." + "read.jdbc.username", prefix + ".jdbc.username", "read.jdbc.username"),
                StringUtils::isNotBlank, String.class, null));
        readDefinitionBuilder.addPropertyValue("password", BeanDefineUtils.fetchProperty(env, Arrays.asList(
                prefix + "." + "read.jdbc.password", prefix + ".jdbc.password", "read.jdbc.password"),
                StringUtils::isNotBlank, String.class, null));
        if (BeanDefinitionRegistryUtils.overideBeanDefinition(context.getRegistry(),
                prefix + "ReadDataSource", readDefinitionBuilder.getBeanDefinition(), true)) {
            log.info("register c3p0.ComboPooledDataSource: {}", prefix + "ReadDataSource");
        }
        // 注册 写 DruidDataSource，beanName="#prefix + 'WriteDataSource'"
        BeanDefinitionBuilder writeDefinitionBuilder =
                BeanDefinitionBuilder.genericBeanDefinition(DataSourceConnectionPoolType.C3P0.getDataSourceClass());
        completeC3P0DataSourceBeanDefinition(writeDefinitionBuilder, env, createDataSourceEnv, "write");
        writeDefinitionBuilder.addPropertyValue("jdbcUrl", BeanDefineUtils.fetchProperty(env,
                Arrays.asList(prefix + "." + "write.jdbc.url", prefix + ".jdbc.url", "write.jdbc.url"),
                StringUtils::isNotBlank, String.class, null));
        writeDefinitionBuilder.addPropertyValue("user", BeanDefineUtils.fetchProperty(env,
                Arrays.asList(prefix + "." + "write.jdbc.username", prefix + ".jdbc.username", "write.jdbc.username"),
                StringUtils::isNotBlank, String.class, null));
        writeDefinitionBuilder.addPropertyValue("password", BeanDefineUtils.fetchProperty(env, Arrays.asList(
                prefix + "." + "write.jdbc.password", prefix + ".jdbc.password", "write.jdbc.password"),
                StringUtils::isNotBlank, String.class, null));
        if (BeanDefinitionRegistryUtils.overideBeanDefinition(context.getRegistry(),
                prefix + "WriteDataSource", writeDefinitionBuilder.getBeanDefinition(), true)) {
            log.info("register c3p0.ComboPooledDataSource: {}", prefix + "WriteDataSource");
        }
    }

    /**
     * 完善 ComboPooledDataSource
     */
    private static void completeC3P0DataSourceBeanDefinition(BeanDefinitionBuilder builder,
                                                             Environment environment,
                                                             CreateDataSourceEnv createDataSourceEnv,
                                                             String readWrite) {
        final Function<String, List<String>> keysFunction = k -> Arrays.asList(
                createDataSourceEnv.getPrefix() + "." + readWrite + "." + k,
                createDataSourceEnv.getPrefix() + "." + k,
                readWrite + "." + k,
                k
        );
        final Map<String, Object> propertyMap = new HashMap<>();
        BeanDefineUtils.setIfAbsent(builder, environment, "driverClass",
                keysFunction.apply("jdbc.driverClass"), null, null, propertyMap);
        if ("com.ibm.db2.jcc.DB2Driver".equals(propertyMap.get("driverClassName"))) {
            createDataSourceEnv.setType("db2");
        }
        // c3p0数据库连接池中初始化时的连接数
        BeanDefineUtils.setIfAbsent(builder, environment, "initialPoolSize",
                keysFunction.apply("jdbc.initialSize"), Integer.class, 0, propertyMap);
        // c3p0数据库连接池维护的最少连接数
        BeanDefineUtils.setIfAbsent(builder, environment, "minPoolSize",
                keysFunction.apply("jdbc.minIdle"), Integer.class, 1, propertyMap);
        // c3p0数据库连接池维护的最多的连接数
        BeanDefineUtils.setIfAbsent(builder, environment, "maxPoolSize",
                keysFunction.apply("jdbc.maxActive"), Integer.class, 5, propertyMap);
        // 当连接池中的连接耗尽的时候c3p0一次同时获取的连接数。Default: 3
        BeanDefineUtils.setIfAbsent(builder, environment, "acquireIncrement",
                keysFunction.apply("jdbc.acquireIncrement"), Integer.class, 3, propertyMap);
        // 当连接池中的连接耗尽的时候c3p0一次同时获取的连接数。Default: 30
        BeanDefineUtils.setIfAbsent(builder, environment, "acquireRetryAttempts",
                keysFunction.apply("jdbc.acquireRetryAttempts"), Integer.class, null, propertyMap);
        // 两次连接中间隔时间，单位毫秒。Default: 1000
        BeanDefineUtils.setIfAbsent(builder, environment, "acquireRetryDelay",
                keysFunction.apply("jdbc.acquireRetryDelay"), Integer.class, 1000, propertyMap);
        // 最大空闲时间,xx秒内未使用则连接被丢弃。若为0则永不丢弃。Default: 0
        // 此处修改默认值为300秒
        BeanDefineUtils.setIfAbsent(builder, environment, "maxIdleTime",
                keysFunction.apply("jdbc.minEvictableIdleTimeMillis"), Integer.class, 300000,
                x -> ((int) Converter.convertToNumber.apply(x, Integer.class)) / 1000, propertyMap);
        // 单位秒，为了减轻连接池的负载，当连接池经过数据访问高峰创建了很多连接，但是后面
        // 连接池不需要维护这么多连接，必须小于maxIdleTime
        // 配置不为0，则将连接池的数量保持到minPoolSize
        BeanDefineUtils.setIfAbsent(builder, environment, "maxIdleTimeExcessConnection",
                keysFunction.apply("jdbc.maxIdleTimeExcessConnection"), Integer.class, null, propertyMap);
        // 当连接池用完时客户端调用getConnection()后等待获取新连接的时间，
        // 超时后将抛出SQLException,如设为0则无限期等待。单位毫秒。Default: 0
        // 此处修改默认值为60秒
        BeanDefineUtils.setIfAbsent(builder, environment, "checkoutTimeout",
                keysFunction.apply("jdbc.maxWait"), Integer.class, 60000, propertyMap);
        // JDBC的标准参数，用以控制数据源内加载的PreparedStatements数量。但由于预缓存的statements
        // 属于单个connection而不是整个连接池。所以设置这个参数需要考虑到多方面的因素。
        // 如果maxStatements与maxStatementsPerConnection均为0，则缓存被关闭。Default: 0
        BeanDefineUtils.setIfAbsent(builder, environment, "maxStatements",
                keysFunction.apply("jdbc.maxOpenPreparedStatements"), Integer.class, null, propertyMap);
        // maxStatementsPerConnection定义了连接池内单个连接所拥有的最大缓存statements数。Default: 0
        BeanDefineUtils.setIfAbsent(builder, environment, "maxStatementsPerConnection",
                keysFunction.apply("jdbc.maxPoolPreparedStatementPerConnectionSize"), Integer.class, null, propertyMap);
        // 定义所有连接测试都执行的测试语句。在使用连接测试的情况下这个一显著提高测试速度。
        // 注意：测试的表必须在初始数据源的时候就存在。Default: null
        // 检查连接的SQL，默认=>SELECT 1, DB2默认=>SELECT 1 FROM SYSIBM.DUAL，可自定义覆盖
        BeanDefineUtils.setIfAbsent(builder, environment, "preferredTestQuery",
                keysFunction.apply("jdbc.validationQuery"), null,
                !"db2".equals(createDataSourceEnv.getType()) ? "SELECT 1" : "SELECT 1 FROM SYSIBM.DUAL",
                propertyMap);
        // 每xx秒检查所有连接池中的空闲连接。Default: 0
        // 此处修改默认值为60秒
        BeanDefineUtils.setIfAbsent(builder, environment, "idleConnectionTestPeriod",
                keysFunction.apply("jdbc.timeBetweenEvictionRunsMillis"), Integer.class, 60000,
                x -> ((int) Converter.convertToNumber.apply(x, Integer.class)) / 1000, propertyMap);
        // 如果设为true那么在取得连接的同时将校验连接的有效性。Default: false
        BeanDefineUtils.setIfAbsent(builder, environment, "testConnectionOnCheckin",
                keysFunction.apply("jdbc.testOnBorrow"), Boolean.class, false, propertyMap);
        // 因性能消耗大请只在需要的时候使用它。如果设为true那么在每个connection提交的
        // 时候都将校验其有效性。建议使用idleConnectionTestPeriod或automaticTestTable
        // 等方法来提升连接测试的性能。Default: false
        BeanDefineUtils.setIfAbsent(builder, environment, "testConnectionOnCheckout",
                keysFunction.apply("jdbc.testOnReturn"), Boolean.class, false, propertyMap);
        // 连接关闭时默认将所有未提交的操作提交，false表示回滚。Default: false
        BeanDefineUtils.setIfAbsent(builder, environment, "autoCommitOnClose",
                keysFunction.apply("jdbc.autoCommitOnClose"), Boolean.class, false, propertyMap);
    }

    /**
     * 注册 HikariDataSource
     */
    private static void registerHikariCP(RegisterBeanDefinitionContext context,
                                         CreateDataSourceEnv createDataSourceEnv) {
        final String prefix = createDataSourceEnv.getPrefix();
        final Environment env = context.getEnvironment();
        // 注册 读 DruidDataSource，beanName="#prefix + 'ReadDataSource'"
        // read bean 配置
        BeanDefinitionBuilder readConfigDefinitionBuilder =
                BeanDefinitionBuilder.genericBeanDefinition("com.zaxxer.hikari.HikariConfig");
        completeHikariCPDataSourceBeanDefinition(readConfigDefinitionBuilder, env, createDataSourceEnv, "read");
        readConfigDefinitionBuilder.addPropertyValue("jdbcUrl", BeanDefineUtils.fetchProperty(env,
                Arrays.asList(prefix + "." + "read.jdbc.url", prefix + ".jdbc.url", "read.jdbc.url"),
                StringUtils::isNotBlank, String.class, null));
        readConfigDefinitionBuilder.addPropertyValue("username", BeanDefineUtils.fetchProperty(env,
                Arrays.asList(prefix + "." + "read.jdbc.username", prefix + ".jdbc.username", "read.jdbc.username"),
                StringUtils::isNotBlank, String.class, null));
        readConfigDefinitionBuilder.addPropertyValue("password", BeanDefineUtils.fetchProperty(env, Arrays.asList(
                prefix + "." + "read.jdbc.password", prefix + ".jdbc.password", "read.jdbc.password"),
                StringUtils::isNotBlank, String.class, null));
        BeanDefinitionRegistryUtils.overideBeanDefinition(context.getRegistry(),
                prefix + "ReadDataSourceConfig", readConfigDefinitionBuilder.getBeanDefinition(), true);
        // read DataSource bean
        BeanDefinitionBuilder readDefinitionBuilder =
                BeanDefinitionBuilder.genericBeanDefinition(DataSourceConnectionPoolType.HikariCP.getDataSourceClass());
        readDefinitionBuilder.addConstructorArgReference(prefix + "ReadDataSourceConfig");
        if (BeanDefinitionRegistryUtils.overideBeanDefinition(context.getRegistry(),
                prefix + "ReadDataSource", readDefinitionBuilder.getBeanDefinition(), true)) {
            log.info("register HikariDataSource: {}", prefix + "ReadDataSource");
        }
        // 注册 写 DruidDataSource，beanName="#prefix + 'WriteDataSource'"
        // write bean 配置
        BeanDefinitionBuilder writeConfigDefinitionBuilder =
                BeanDefinitionBuilder.genericBeanDefinition("com.zaxxer.hikari.HikariConfig");
        completeHikariCPDataSourceBeanDefinition(writeConfigDefinitionBuilder, env, createDataSourceEnv, "write");
        writeConfigDefinitionBuilder.addPropertyValue("jdbcUrl", BeanDefineUtils.fetchProperty(env,
                Arrays.asList(prefix + "." + "write.jdbc.url", prefix + ".jdbc.url", "write.jdbc.url"),
                StringUtils::isNotBlank, String.class, null));
        writeConfigDefinitionBuilder.addPropertyValue("username", BeanDefineUtils.fetchProperty(env,
                Arrays.asList(prefix + "." + "write.jdbc.username", prefix + ".jdbc.username", "write.jdbc.username"),
                StringUtils::isNotBlank, String.class, null));
        writeConfigDefinitionBuilder.addPropertyValue("password", BeanDefineUtils.fetchProperty(env, Arrays.asList(
                prefix + "." + "write.jdbc.password", prefix + ".jdbc.password", "write.jdbc.password"),
                StringUtils::isNotBlank, String.class, null));
        BeanDefinitionRegistryUtils.overideBeanDefinition(context.getRegistry(),
                prefix + "WriteDataSourceConfig", writeConfigDefinitionBuilder.getBeanDefinition(), true);
        // write DataSource bean
        BeanDefinitionBuilder writeDefinitionBuilder =
                BeanDefinitionBuilder.genericBeanDefinition(DataSourceConnectionPoolType.HikariCP.getDataSourceClass());
        writeDefinitionBuilder.addConstructorArgReference(prefix + "WriteDataSourceConfig");
        if (BeanDefinitionRegistryUtils.overideBeanDefinition(context.getRegistry(),
                prefix + "WriteDataSource", writeDefinitionBuilder.getBeanDefinition(), true)) {
            log.info("register HikariDataSource: {}", prefix + "WriteDataSource");
        }
    }

    /**
     * 完善 HikariDataSource
     */
    private static void completeHikariCPDataSourceBeanDefinition(BeanDefinitionBuilder builder,
                                                                 Environment environment,
                                                                 CreateDataSourceEnv createDataSourceEnv,
                                                                 String readWrite) {
        final Function<String, List<String>> keysFunction = k -> Arrays.asList(
                createDataSourceEnv.getPrefix() + "." + readWrite + "." + k,
                createDataSourceEnv.getPrefix() + "." + k,
                readWrite + "." + k,
                k
        );
        final Map<String, Object> propertyMap = new HashMap<>();
        BeanDefineUtils.setIfAbsent(builder, environment, "driverClassName",
                keysFunction.apply("jdbc.driverClass"), null, null, propertyMap);
        if ("com.ibm.db2.jcc.DB2Driver".equals(propertyMap.get("driverClassName"))) {
            createDataSourceEnv.setType("db2");
        }
        // 此属性控制HikariCP尝试在池中维护的最小空闲连接数。如果空闲连接下降到该值以下，
        // 并且池中的总连接数少于maximumPoolSize，则HikariCP将尽最大努力快速而有效地添加其他连接
        BeanDefineUtils.setIfAbsent(builder, environment, "minimumIdle",
                keysFunction.apply("jdbc.minIdle"), Integer.class, 1, propertyMap);
        // 此属性控制允许池达到的最大大小，包括空闲和使用中的连接。基本上，此值将确定到数据库后端的最大实际连接数
        BeanDefineUtils.setIfAbsent(builder, environment, "maximumPoolSize",
                keysFunction.apply("jdbc.maxActive"), Integer.class, 5, propertyMap);
        // 等待来自池的连接的最大毫秒数。如果超过此时间而没有可用的连接，则会抛出SQLException。
        // 可接受的最低连接超时为250 ms。 默认值：30000（30秒）
        BeanDefineUtils.setIfAbsent(builder, environment, "connectionTimeout",
                keysFunction.apply("jdbc.maxWait"), Long.class, 60000L, propertyMap);
        // 此属性控制允许连接在池中保持空闲状态的最长时间
        BeanDefineUtils.setIfAbsent(builder, environment, "idleTimeout",
                keysFunction.apply("jdbc.maxEvictableIdleTimeMillis"), Long.class, 300000L, propertyMap);
        // 该属性用于控制连接在池中的最大生存时间，超过该时间强制逐出，连接池向数据申请新的连接进行补充。
        // 注意：当前正在使用的连接不会强制逐出，哪怕它的累计时间已经到了maxLifetime。缺省：1800000， 即30min
        BeanDefineUtils.setIfAbsent(builder, environment, "maxLifetime",
                keysFunction.apply("jdbc.maxEvictableIdleTimeMillis"), Long.class, null, propertyMap);
        // 检查连接的SQL，默认=>SELECT 1, DB2默认=>SELECT 1 FROM SYSIBM.DUAL，可自定义覆盖
        BeanDefineUtils.setIfAbsent(builder, environment, "connectionTestQuery",
                keysFunction.apply("jdbc.validationQuery"), null,
                !"db2".equals(createDataSourceEnv.getType()) ? "SELECT 1" : "SELECT 1 FROM SYSIBM.DUAL",
                propertyMap);
        // 控制将测试连接的活动性的最长时间，可接受的最低验证超时为250毫秒。 默认值：5000
        BeanDefineUtils.setIfAbsent(builder, environment, "validationTimeout",
                keysFunction.apply("jdbc.validationQueryTimeout"), String.class, null,
                x -> ((BigDecimal) Converter.convertToNumber.apply(x, BigDecimal.class))
                        .multiply(new BigDecimal(1000)).longValue(), propertyMap);
        // 用于跟数据库保持心跳连接，防止底层网络基础设施超时断开，定期验证连接的有效性
        BeanDefineUtils.setIfAbsent(builder, environment, "keepaliveTime",
                keysFunction.apply("jdbc.keepAliveBetweenTimeMillis"), Long.class, 60000L, propertyMap);
    }

}
