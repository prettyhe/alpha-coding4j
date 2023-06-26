package com.alpha.coding.common.datasource;

import java.util.Arrays;
import java.util.Set;

import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.util.ClassUtils;

import com.alpha.coding.common.bean.register.BeanDefineUtils;
import com.alpha.coding.common.bean.register.EnvironmentChangeEvent;
import com.alpha.coding.common.bean.register.EnvironmentChangeListener;
import com.alpha.coding.common.bean.spi.ConfigurationRegisterHandler;
import com.alpha.coding.common.bean.spi.RegisterBeanDefinitionContext;
import com.alpha.coding.common.utils.SpringAnnotationConfigUtils;
import com.alpha.coding.common.utils.StringUtils;

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
        if (Arrays.stream(DataSourceConnectionPoolType.values())
                .allMatch(p -> StringUtils.isBlank(p.getDataSourceClass())
                        || !ClassUtils.isPresent(p.getDataSourceClass(),
                        Thread.currentThread().getContextClassLoader()))) {
            log.warn("None supported DataSource class is present in classpath, consider Druid/C3P0/HikariCP");
            return;
        }
        Set<AnnotationAttributes> annotationAttributes = SpringAnnotationConfigUtils.attributesForRepeatable(
                context.getImportingClassMetadata(), EnableAutoDataSources.class, EnableAutoDataSource.class);
        if (annotationAttributes.isEmpty()) {
            return;
        }
        for (AnnotationAttributes attributes : annotationAttributes) {
            DataSourceRegisterUtils.register(context, new CreateDataSourceEnv()
                    .setPrefix(attributes.getString("prefix"))
                    .setType(BeanDefineUtils.resolveValue(context, attributes.getString("type"), String.class))
                    .setConnectionPoolType(attributes.getEnum("connectionPoolType")));
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
