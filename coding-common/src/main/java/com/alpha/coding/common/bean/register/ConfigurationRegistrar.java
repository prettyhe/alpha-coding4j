package com.alpha.coding.common.bean.register;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
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

    private ConfigurationRegisterHandler handler = ServiceBootstrap.loadPrimary(ConfigurationRegisterHandler.class);

    @Setter
    private ResourceLoader resourceLoader;
    @Setter
    private BeanFactory beanFactory;
    @Setter
    private Environment environment;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        handler.registerBeanDefinitions(new RegisterBeanDefinitionContext()
                .setImportingClassMetadata(importingClassMetadata)
                .setRegistry(registry)
                .setResourceLoader(resourceLoader)
                .setBeanFactory(beanFactory)
                .setEnvironment(environment));
    }
}
