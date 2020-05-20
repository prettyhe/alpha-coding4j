package com.alpha.coding.common.bean.spi;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * RegisterBeanDefinitionContext
 *
 * @version 1.0
 * Date: 2020-03-19
 */
@Data
@Accessors(chain = true)
public class RegisterBeanDefinitionContext {

    private AnnotationMetadata importingClassMetadata;
    private BeanDefinitionRegistry registry;
    private ResourceLoader resourceLoader;
    private BeanFactory beanFactory;
    private Environment environment;

}
