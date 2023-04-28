package com.alpha.coding.common.mybatis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.Configuration;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.util.ClassUtils;

import com.alpha.coding.common.bean.register.BeanDefineUtils;
import com.alpha.coding.common.bean.register.BeanDefinitionRegistryUtils;
import com.alpha.coding.common.bean.register.EnvironmentChangeEvent;
import com.alpha.coding.common.bean.register.EnvironmentChangeListener;
import com.alpha.coding.common.bean.spi.ConfigurationRegisterHandler;
import com.alpha.coding.common.bean.spi.RegisterBeanDefinitionContext;
import com.alpha.coding.common.datasource.CreateDataSourceEnv;
import com.alpha.coding.common.datasource.DataSourceRegisterUtils;
import com.alpha.coding.common.utils.ListUtils;
import com.alpha.coding.common.utils.SpringAnnotationConfigUtils;
import com.alpha.coding.common.utils.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * EnableMybatisAutoConfigHandler
 *
 * @version 1.0
 * Date: 2020-03-21
 */
@Slf4j
public class EnableMybatisAutoConfigHandler implements ConfigurationRegisterHandler, EnvironmentChangeListener {

    @Override
    public void registerBeanDefinitions(RegisterBeanDefinitionContext context) {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (!ClassUtils.isPresent("org.mybatis.spring.SqlSessionFactoryBean", classLoader)) {
            log.warn("SqlSessionFactoryBean is not present in classpath");
            return;
        }
        Set<AnnotationAttributes> annotationAttributes = SpringAnnotationConfigUtils.attributesForRepeatable(
                context.getImportingClassMetadata(), EnableMybatisAutoConfigs.class, EnableMybatisAutoConfig.class);
        if (CollectionUtils.isEmpty(annotationAttributes)) {
            return;
        }
        final BeanDefinitionRegistry registry = context.getRegistry();
        if (ClassUtils.isPresent("com.github.miemiedev.mybatis.paginator.OffsetLimitInterceptor", classLoader)) {
            // 注册 pageHandlerInterceptor
            BeanDefinitionBuilder pageDefinitionBuilder = BeanDefinitionBuilder
                    .genericBeanDefinition("com.github.miemiedev.mybatis.paginator.OffsetLimitInterceptor");
            Properties properties = new Properties();
            properties.put("dialectClass", "com.github.miemiedev.mybatis.paginator.dialect.MySQLDialect");
            pageDefinitionBuilder.addPropertyValue("properties", properties);
            BeanDefinitionRegistryUtils.overideBeanDefinition(registry,
                    "auto_PageHandlerInterceptor",
                    pageDefinitionBuilder.getBeanDefinition());
        }
        // 注册 showSqlInterceptor
        BeanDefinitionBuilder showSQLDefinitionBuilder =
                BeanDefinitionBuilder.genericBeanDefinition(ShowSqlInterceptor.class);
        final Properties showSQLProperties = new Properties();
        showSQLProperties.put("sqlIdAbbreviated", true);
        showSQLDefinitionBuilder.addPropertyValue("properties", showSQLProperties);
        BeanDefinitionRegistryUtils.overideBeanDefinition(registry,
                "auto_ShowSqlInterceptor", showSQLDefinitionBuilder.getBeanDefinition());
        // 注册mybatis相关配置
        for (AnnotationAttributes attributes : annotationAttributes) {
            final AnnotationAttributes dataSource = attributes.getAnnotation("dataSource");
            final String prefix = dataSource.getString("prefix");
            // 注册 读 & 写 DataSource
            DataSourceRegisterUtils.register(context, new CreateDataSourceEnv().setPrefix(prefix)
                    .setType(BeanDefineUtils.resolveValue(context, dataSource.getString("type"), String.class))
                    .setConnectionPoolType(dataSource.getEnum("connectionPoolType")));
            // 注册 DynamicDataSource
            BeanDefinitionBuilder dataSourceBeanDefinitionBuilder =
                    BeanDefinitionBuilder.genericBeanDefinition(DynamicDataSource.class);
            dataSourceBeanDefinitionBuilder.addPropertyReference("readDataSource",
                    prefix + "ReadDataSource");
            dataSourceBeanDefinitionBuilder.addPropertyReference("writeDataSource",
                    prefix + "WriteDataSource");
            BeanDefinitionRegistryUtils.overideBeanDefinition(registry,
                    prefix + "DataSource",
                    dataSourceBeanDefinitionBuilder.getBeanDefinition());
            log.info("register DynamicDataSource: {}", prefix + "DataSource");
            // 此配置标签
            final String tag = attributes.getString("tag");
            // 注册 SqlSessionFactoryBean
            BeanDefinitionBuilder sqlSessionFactoryBeanBuilder = BeanDefinitionBuilder
                    .genericBeanDefinition(SqlSessionFactoryBean.class);
            sqlSessionFactoryBeanBuilder.addPropertyReference("dataSource", prefix + "DataSource");
            final String configuration = attributes.getString("configuration");
            if (StringUtils.isBlank(configuration)) {
                if (StringUtils.isNotBlank(attributes.getString("configLocation"))) {
                    sqlSessionFactoryBeanBuilder.addPropertyValue("configLocation",
                            attributes.getString("configLocation"));
                } else {
                    BeanDefinitionBuilder configurationBeanDefinitionBuilder = BeanDefinitionBuilder
                            .genericBeanDefinition(Configuration.class);
                    configurationBeanDefinitionBuilder.addPropertyValue("mapUnderscoreToCamelCase", true);
                    BeanDefinitionRegistryUtils.overideBeanDefinition(registry,
                            prefix + "_ibatisSessionConfiguration" + tag,
                            configurationBeanDefinitionBuilder.getBeanDefinition());
                    sqlSessionFactoryBeanBuilder.addPropertyReference("configuration",
                            prefix + "_ibatisSessionConfiguration" + tag);
                }
            } else {
                sqlSessionFactoryBeanBuilder.addPropertyReference("configuration", configuration);
            }
            sqlSessionFactoryBeanBuilder.addPropertyValue("mapperLocations",
                    attributes.getStringArray("mapperLocations"));
            if (StringUtils.isNotBlank(attributes.getString("typeAliasesPackage"))) {
                sqlSessionFactoryBeanBuilder.addPropertyValue("typeAliasesPackage",
                        attributes.getString("typeAliasesPackage"));
            }
            final List<String> plugins = new ArrayList<>();
            // 注册 dynamicPlugin
            BeanDefinitionBuilder dynamicPluginDefinitionBuilder =
                    BeanDefinitionBuilder.genericBeanDefinition(DynamicPlugin.class);
            final String[] forceUseWriteDataSourceSqls = attributes.getStringArray("forceUseWriteDataSourceSql");
            if (forceUseWriteDataSourceSqls != null && forceUseWriteDataSourceSqls.length > 0) {
                final Properties dynamicPluginProperties = new Properties();
                dynamicPluginProperties.put("forceUseWriteDataSourceSql", forceUseWriteDataSourceSqls);
                dynamicPluginDefinitionBuilder.addPropertyValue("properties", dynamicPluginProperties);
            }
            BeanDefinitionRegistryUtils.overideBeanDefinition(registry,
                    prefix + "DynamicPlugin" + tag,
                    dynamicPluginDefinitionBuilder.getBeanDefinition());
            plugins.add(prefix + "DynamicPlugin" + tag);
            if (attributes.getBoolean("enablePageHandlerInterceptor")) {
                plugins.add("auto_PageHandlerInterceptor");
            }
            if (attributes.getBoolean("enableShowSqlInterceptor")) {
                plugins.add("auto_ShowSqlInterceptor");
            }
            plugins.addAll(Arrays.asList(attributes.getStringArray("extPlugins")));
            sqlSessionFactoryBeanBuilder.addPropertyValue("plugins", ListUtils.toArray(plugins.stream()
                    .map(x -> new InterceptorWrapper(() -> context.getBeanFactory().getBean(x, Interceptor.class)))
                    .collect(Collectors.toList()), InterceptorWrapper.class));
            BeanDefinitionRegistryUtils.overideBeanDefinition(registry,
                    prefix + "SqlSessionFactory" + tag,
                    sqlSessionFactoryBeanBuilder.getBeanDefinition());
            log.info("register SqlSessionFactory: {}", prefix + "SqlSessionFactory" + tag);
            // 注册 MapperScannerConfigurer
            BeanDefinitionBuilder mapperScannerConfigurerBuilder = BeanDefinitionBuilder
                    .genericBeanDefinition(MapperScannerConfigurer.class);
            mapperScannerConfigurerBuilder.addPropertyValue("sqlSessionFactoryBeanName",
                    prefix + "SqlSessionFactory" + tag);
            mapperScannerConfigurerBuilder.addPropertyValue("basePackage",
                    attributes.getString("mapperBasePackage"));
            BeanDefinitionRegistryUtils.overideBeanDefinition(registry,
                    prefix + "MapperScannerConfigurer" + tag,
                    mapperScannerConfigurerBuilder.getBeanDefinition());
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public void onChange(EnvironmentChangeEvent event) {
        final RegisterBeanDefinitionContext context = (RegisterBeanDefinitionContext) event.getSource();
        registerBeanDefinitions(context);
    }

}
