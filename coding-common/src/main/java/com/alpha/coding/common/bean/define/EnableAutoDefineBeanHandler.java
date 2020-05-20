package com.alpha.coding.common.bean.define;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.annotation.AnnotationAttributes;

import com.alpha.coding.common.bean.spi.ConfigurationRegisterHandler;
import com.alpha.coding.common.bean.spi.RegisterBeanDefinitionContext;
import com.alpha.coding.common.utils.SpringAnnotationConfigUtils;

/**
 * EnableAutoDefineBeanHandler
 *
 * @version 1.0
 * Date: 2020-03-19
 */
public class EnableAutoDefineBeanHandler implements ConfigurationRegisterHandler {

    private static final AtomicInteger COUNT = new AtomicInteger(0);

    @Override
    public void registerBeanDefinitions(RegisterBeanDefinitionContext context) {
        Set<AnnotationAttributes> annotationAttributes = SpringAnnotationConfigUtils.attributesForRepeatable(
                context.getImportingClassMetadata(), EnableAutoDefineBeans.class, EnableAutoDefineBean.class);
        if (CollectionUtils.isEmpty(annotationAttributes)) {
            return;
        }
        for (AnnotationAttributes attribute : annotationAttributes) {
            final String[] basePackages = attribute.getStringArray("basePackages");
            if (basePackages != null && basePackages.length > 0) {
                registerBean(context.getRegistry(), new DefineBeanFactory(basePackages));
            }
            final Class<?>[] basePackageClasses = attribute.getClassArray("basePackageClasses");
            if (basePackageClasses != null && basePackageClasses.length > 0) {
                registerBean(context.getRegistry(), new DefineBeanFactory(basePackageClasses));
            }
        }
    }

    private void registerBean(BeanDefinitionRegistry registry, DefineBeanFactory factory) {
        String beanName = DefineBeanFactory.class.getName() + "_AUTO_" + COUNT.incrementAndGet();
        BeanDefinitionBuilder beanDefinitionBuilder =
                BeanDefinitionBuilder.genericBeanDefinition(DefineBeanFactory.class);
        beanDefinitionBuilder.addPropertyValue("beanDefineMap", factory.getBeanDefineMap());
        registry.registerBeanDefinition(beanName, beanDefinitionBuilder.getRawBeanDefinition());
    }

    @Override
    public int getOrder() {
        return 0;
    }

}
