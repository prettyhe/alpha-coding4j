package com.alpha.coding.common.bean.register;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;

import com.alpha.coding.common.bean.spi.ConfigurationRegisterHandler;
import com.alpha.coding.common.bean.spi.RegisterBeanDefinitionContext;
import com.alpha.coding.common.bean.spi.ServiceBootstrap;

import lombok.Setter;

/**
 * ConfigurationRegistrar
 *
 * @version 1.0
 * Date: 2020-03-18
 */
public class ConfigurationRegistrar implements ImportBeanDefinitionRegistrar,
        ResourceLoaderAware, BeanFactoryAware, EnvironmentAware {

    private static final DefaultBeanNameGenerator BEAN_NAME_GENERATOR = new DefaultBeanNameGenerator();
    private static final AtomicInteger TOTAL_COUNT = new AtomicInteger(0);

    private ConfigurationRegisterHandler handler = ServiceBootstrap.loadPrimary(ConfigurationRegisterHandler.class);

    @Setter
    private ResourceLoader resourceLoader;
    @Setter
    private BeanFactory beanFactory;
    @Setter
    private Environment environment;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        TOTAL_COUNT.incrementAndGet();
        if (environment instanceof ConfigurableEnvironment) {
            ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) environment;
            final Set<String> propertySourceNames = new LinkedHashSet<>();
            configurableEnvironment.getPropertySources().forEach(s -> propertySourceNames.add(s.getName()));
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
                    .genericBeanDefinition(EnvironmentPropertySourcesChangeBeanFactoryProcessor.class);
            beanDefinitionBuilder.addPropertyValue("environment", environment);
            beanDefinitionBuilder.addPropertyValue("propertySourceNames", propertySourceNames);
            beanDefinitionBuilder.addPropertyValue("initIndex", TOTAL_COUNT.get() - 1);
            beanDefinitionBuilder.addPropertyValue("globalIndex", TOTAL_COUNT);
            BiConsumer<ConfigurableListableBeanFactory, Environment> onChanged = (fac, env) -> {
                if (handler instanceof EnvironmentChangeListener) {
                    EnvironmentChangeListener listener = (EnvironmentChangeListener) handler;
                    listener.onChange(new EnvironmentChangeEvent(new RegisterBeanDefinitionContext()
                            .setImportingClassMetadata(importingClassMetadata)
                            .setRegistry(registry)
                            .setResourceLoader(resourceLoader)
                            .setBeanFactory(beanFactory)
                            .setEnvironment(environment)));
                }
            };
            beanDefinitionBuilder.addPropertyValue("onChanged", onChanged);
            final BeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
            registry.registerBeanDefinition(BEAN_NAME_GENERATOR.generateBeanName(beanDefinition, registry),
                    beanDefinition);
        }
        handler.registerBeanDefinitions(new RegisterBeanDefinitionContext()
                .setImportingClassMetadata(importingClassMetadata)
                .setRegistry(registry)
                .setResourceLoader(resourceLoader)
                .setBeanFactory(beanFactory)
                .setEnvironment(environment));
    }
}
