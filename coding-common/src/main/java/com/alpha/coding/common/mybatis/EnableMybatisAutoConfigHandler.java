package com.alpha.coding.common.mybatis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.util.ClassUtils;

import com.alpha.coding.common.bean.spi.ConfigurationRegisterHandler;
import com.alpha.coding.common.bean.spi.RegisterBeanDefinitionContext;
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
public class EnableMybatisAutoConfigHandler implements ConfigurationRegisterHandler {

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
        if (ClassUtils.isPresent("com.github.miemiedev.mybatis.paginator.OffsetLimitInterceptor", classLoader)) {
            // 注册 pageHandlerInterceptor
            BeanDefinitionBuilder pageDefinitionBuilder = BeanDefinitionBuilder
                    .genericBeanDefinition("com.github.miemiedev.mybatis.paginator.OffsetLimitInterceptor");
            Properties properties = new Properties();
            properties.put("dialectClass", "com.github.miemiedev.mybatis.paginator.dialect.MySQLDialect");
            pageDefinitionBuilder.addPropertyValue("properties", properties);
            context.getRegistry().registerBeanDefinition("auto_PageHandlerInterceptor",
                    pageDefinitionBuilder.getBeanDefinition());
        }
        // 注册 showSqlInterceptor
        BeanDefinitionBuilder showSQLDefinitionBuilder =
                BeanDefinitionBuilder.genericBeanDefinition(ShowSqlInterceptor.class);
        final Properties showSQLProperties = new Properties();
        showSQLProperties.put("sqlIdAbbreviated", true);
        showSQLDefinitionBuilder.addPropertyValue("properties", showSQLProperties);
        context.getRegistry().registerBeanDefinition("auto_ShowSqlInterceptor",
                showSQLDefinitionBuilder.getBeanDefinition());
        // 注册 dynamicPlugin
        BeanDefinitionBuilder dynamicPluginDefinitionBuilder =
                BeanDefinitionBuilder.genericBeanDefinition(DynamicPlugin.class);
        context.getRegistry().registerBeanDefinition("auto_DynamicPlugin",
                dynamicPluginDefinitionBuilder.getBeanDefinition());
        // 注册mybatis相关配置
        for (AnnotationAttributes attributes : annotationAttributes) {
            final AnnotationAttributes dataSource = attributes.getAnnotation("dataSource");
            final String prefix = dataSource.getString("prefix");
            // 注册 读 & 写 DataSource
            DataSourceRegisterUtils.register(context, prefix);
            // 注册 DynamicDataSource
            BeanDefinitionBuilder dataSourceBeanDefinitionBuilder =
                    BeanDefinitionBuilder.genericBeanDefinition(DynamicDataSource.class);
            dataSourceBeanDefinitionBuilder.addPropertyReference("readDataSource",
                    prefix + "ReadDataSource");
            dataSourceBeanDefinitionBuilder.addPropertyReference("writeDataSource",
                    prefix + "WriteDataSource");
            context.getRegistry().registerBeanDefinition(prefix + "DataSource",
                    dataSourceBeanDefinitionBuilder.getBeanDefinition());
            log.info("register DynamicDataSource: {}", prefix + "DataSource");
            // 注册 SqlSessionFactoryBean
            BeanDefinitionBuilder sqlSessionFactoryBeanBuilder = BeanDefinitionBuilder
                    .genericBeanDefinition("org.mybatis.spring.SqlSessionFactoryBean");
            sqlSessionFactoryBeanBuilder.addPropertyReference("dataSource", prefix + "DataSource");
            final String configuration = attributes.getString("configuration");
            if (StringUtils.isBlank(configuration)) {
                BeanDefinitionBuilder configurationBeanDefinitionBuilder = BeanDefinitionBuilder
                        .genericBeanDefinition("org.apache.ibatis.session.Configuration");
                configurationBeanDefinitionBuilder.addPropertyValue("mapUnderscoreToCamelCase", true);
                context.getRegistry().registerBeanDefinition(prefix + "_ibatisSessionConfiguration",
                        configurationBeanDefinitionBuilder.getBeanDefinition());
                sqlSessionFactoryBeanBuilder.addPropertyReference("configuration",
                        prefix + "_ibatisSessionConfiguration");
            } else {
                sqlSessionFactoryBeanBuilder.addPropertyReference("configuration", configuration);
            }
            sqlSessionFactoryBeanBuilder.addPropertyValue("mapperLocations",
                    attributes.getStringArray("mapperLocations"));
            sqlSessionFactoryBeanBuilder.addPropertyValue("typeAliasesPackage",
                    attributes.getString("typeAliasesPackage"));
            final List<String> plugins = new ArrayList<>();
            plugins.add("auto_DynamicPlugin");
            if (attributes.getBoolean("enablePageHandlerInterceptor")) {
                plugins.add("auto_PageHandlerInterceptor");
            }
            if (attributes.getBoolean("enableShowSqlInterceptor")) {
                plugins.add("auto_ShowSqlInterceptor");
            }
            Arrays.stream(attributes.getStringArray("extPlugins")).forEach(x -> plugins.add(x));
            sqlSessionFactoryBeanBuilder.addPropertyValue("plugins", ListUtils.toArray(plugins.stream()
                    .map(x -> context.getBeanFactory().getBean(x))
                    .collect(Collectors.toList()), loadClass("org.apache.ibatis.plugin.Interceptor")));
            context.getRegistry().registerBeanDefinition(prefix + "SqlSessionFactory",
                    sqlSessionFactoryBeanBuilder.getBeanDefinition());
            log.info("register SqlSessionFactory: {}", prefix + "SqlSessionFactory");
            // 注册 MapperScannerConfigurer
            BeanDefinitionBuilder mapperScannerConfigurerBuilder = BeanDefinitionBuilder
                    .genericBeanDefinition("org.mybatis.spring.mapper.MapperScannerConfigurer");
            mapperScannerConfigurerBuilder.addPropertyValue("sqlSessionFactoryBeanName",
                    prefix + "SqlSessionFactory");
            mapperScannerConfigurerBuilder.addPropertyValue("basePackage",
                    attributes.getString("mapperBasePackage"));
            context.getRegistry().registerBeanDefinition(prefix + "MapperScannerConfigurer",
                    mapperScannerConfigurerBuilder.getBeanDefinition());
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private Class loadClass(String name) {
        try {
            return com.alpha.coding.common.utils.ClassUtils.loadClass(name, true);
        } catch (ClassNotFoundException e) {
            log.warn("{} not found", name);
            throw new RuntimeException(e);
        }
    }
}
