package com.alpha.coding.common.bean.identity;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

import com.alpha.coding.common.bean.spi.ConfigurationRegisterHandler;
import com.alpha.coding.common.bean.spi.RegisterBeanDefinitionContext;
import com.alpha.coding.common.utils.SpringAnnotationConfigUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * EnableAutoIdentityBeanHandler
 *
 * @version 1.0
 * Date: 2020/8/4
 */
@Slf4j
public class EnableAutoIdentityBeanHandler implements ConfigurationRegisterHandler {

    private final Map<Class<? extends BeanNameGenerator>, BeanNameGenerator> generatorMap = new HashMap<>();
    private final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    private final MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory((ResourceLoader) null);
    private final List<String> registeredBeanNames = new ArrayList<>();

    @Override
    public void registerBeanDefinitions(RegisterBeanDefinitionContext context) {
        Set<AnnotationAttributes> annotationAttributes = SpringAnnotationConfigUtils.attributesForRepeatable(
                context.getImportingClassMetadata(), EnableAutoIdentityBeans.class, EnableAutoIdentityBean.class);
        if (annotationAttributes.isEmpty()) {
            return;
        }
        for (AnnotationAttributes attribute : annotationAttributes) {
            final String[] basePackages = attribute.getStringArray("basePackages");
            if (basePackages != null && basePackages.length > 0) {
                final Class<?>[] excludes = attribute.getClassArray("excludes");
                final Class<? extends BeanNameGenerator> generatorClass = attribute.getClass("beanNameGenerator");
                final BeanNameGenerator beanNameGenerator = generatorMap.computeIfAbsent(generatorClass, k -> {
                    try {
                        return k.newInstance();
                    } catch (Exception e) {
                        throw new BeanCreationException("No such BeanNameGenerator: " + generatorClass.getName());
                    }
                });
                registerBean(context.getRegistry(), basePackages, excludes, beanNameGenerator);
            }
        }
    }

    private synchronized void registerBean(BeanDefinitionRegistry registry, String[] basePackages, Class<?>[] excludes,
                                           BeanNameGenerator beanNameGenerator) {
        try {
            for (String basePackage : basePackages) {
                String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                        + ClassUtils.convertClassNameToResourcePath(new StandardEnvironment()
                        .resolveRequiredPlaceholders(basePackage)) + '/' + "**/*.class";
                Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
                for (Resource resource : resources) {
                    if (log.isTraceEnabled()) {
                        log.trace("Scanning " + resource);
                    }
                    if (resource.isReadable()) {
                        try {
                            MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                            final String className = metadataReader.getClassMetadata().getClassName();
                            final Class<?> beanClazz =
                                    Thread.currentThread().getContextClassLoader().loadClass(className);
                            if (beanClazz.isInterface() || Modifier.isAbstract(beanClazz.getModifiers())) {
                                continue;
                            }
                            if (!com.alpha.coding.common.bean.identity.IdentityBean.class.isAssignableFrom(beanClazz)
                                    && !metadataReader.getAnnotationMetadata().hasAnnotation(
                                    com.alpha.coding.common.bean.identity.annotation.IdentityBean.class.getName())
                                    && AnnotationUtils.findAnnotation(beanClazz,
                                    com.alpha.coding.common.bean.identity.annotation.IdentityBean.class) == null) {
                                continue;
                            }
                            if (excludes != null && Arrays.asList(excludes).contains(beanClazz)) {
                                continue;
                            }
                            final BeanDefinitionBuilder beanDefinitionBuilder =
                                    BeanDefinitionBuilder.genericBeanDefinition(beanClazz);
                            final AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
                            final String beanName = beanNameGenerator.generateBeanName(beanDefinition, registry);
                            if (!registeredBeanNames.contains(beanName)) {
                                registry.registerBeanDefinition(beanName, beanDefinition);
                                log.info("registerBeanDefinition: beanName={},type={}", beanName, beanClazz.getName());
                                registeredBeanNames.add(beanName);
                            }
                        } catch (Throwable ex) {
                            throw new FatalBeanException("Failed to load BeanDefine class: " + resource, ex);
                        }
                    } else {
                        if (log.isTraceEnabled()) {
                            log.trace("Ignored because not readable: " + resource);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new BeanDefinitionStoreException("I/O failure during classpath scanning", e);
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }

}
