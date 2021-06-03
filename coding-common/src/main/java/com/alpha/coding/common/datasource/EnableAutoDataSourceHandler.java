package com.alpha.coding.common.datasource;

import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.util.ClassUtils;

import com.alpha.coding.common.bean.register.BeanDefineUtils;
import com.alpha.coding.common.bean.register.EnvironmentChangeEvent;
import com.alpha.coding.common.bean.register.EnvironmentChangeListener;
import com.alpha.coding.common.bean.spi.ConfigurationRegisterHandler;
import com.alpha.coding.common.bean.spi.RegisterBeanDefinitionContext;
import com.alpha.coding.common.utils.SpringAnnotationConfigUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * EnableAutoDataSourceHandler
 *
 * @version 1.0
 * Date: 2020-03-20
 */
@Slf4j
public class EnableAutoDataSourceHandler implements ConfigurationRegisterHandler, EnvironmentChangeListener {

    @Override
    public void registerBeanDefinitions(RegisterBeanDefinitionContext context) {
        if (!ClassUtils.isPresent("com.alibaba.druid.pool.DruidDataSource",
                Thread.currentThread().getContextClassLoader())) {
            log.warn("DruidDataSource is not present in classpath");
            return;
        }
        Set<AnnotationAttributes> annotationAttributes = SpringAnnotationConfigUtils.attributesForRepeatable(
                context.getImportingClassMetadata(), EnableAutoDataSources.class, EnableAutoDataSource.class);
        if (CollectionUtils.isEmpty(annotationAttributes)) {
            return;
        }
        for (AnnotationAttributes attributes : annotationAttributes) {
            DataSourceRegisterUtils.register(context, new CreateDataSourceEnv()
                    .setPrefix(attributes.getString("prefix"))
                    .setType(BeanDefineUtils.resolveValue(context, attributes.getString("type"), String.class)));
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
