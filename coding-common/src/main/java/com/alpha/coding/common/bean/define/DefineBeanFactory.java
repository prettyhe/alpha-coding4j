package com.alpha.coding.common.bean.define;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.yaml.snakeyaml.Yaml;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alpha.coding.bo.base.Tuple;
import com.alpha.coding.common.utils.IOUtils;
import com.alpha.coding.common.utils.StringUtils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * DefineBeanFactory
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class DefineBeanFactory implements ApplicationContextAware, InitializingBean {

    @Setter
    private ApplicationContext applicationContext;

    @Setter
    @Getter
    private Multimap<Class<?>, DefineBeanConfig> beanDefineMap = ArrayListMultimap.create();

    private Map<String, Object> nameBeanMap = new HashMap<>();

    private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    private MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory((ResourceLoader) null);

    public DefineBeanFactory() {
    }

    public DefineBeanFactory(String[] basePackagesArray) {
        scanConfigBeanDefine(basePackagesArray);
    }

    public DefineBeanFactory(Class[] baseClassArray) {
        if (baseClassArray != null && baseClassArray.length > 0) {
            for (Class<?> clz : baseClassArray) {
                final BeanDefine define = clz.getAnnotation(BeanDefine.class);
                if (define == null) {
                    continue;
                }
                beanDefineMap.put(clz, new DefineBeanConfig().setType(define.type())
                        .setBeanName(define.name()).setSrcLocation(define.src()));
            }
        }
    }

    public DefineBeanFactory(String[] basePackagesArray, List<Tuple<?, DefineBeanConfig>> configList)
            throws ClassNotFoundException {
        scanConfigBeanDefine(basePackagesArray);
        if (configList != null && configList.size() > 0) {
            for (Tuple<?, DefineBeanConfig> tuple : configList) {
                Class clz = null;
                if (tuple.getF() instanceof Class) {
                    clz = (Class) tuple.getF();
                } else if (tuple.getF() instanceof String) {
                    clz = Class.forName(String.valueOf(tuple.getF()), true,
                            Thread.currentThread().getContextClassLoader());
                } else {
                    throw new IllegalArgumentException("tuple.f must explain to a Class");
                }
                beanDefineMap.put(clz, tuple.getS());
            }
        }
    }

    public DefineBeanFactory(Multimap<Class<?>, DefineBeanConfig> defineClassMultimap) {
        if (defineClassMultimap != null && defineClassMultimap.size() > 0) {
            beanDefineMap.putAll(defineClassMultimap);
        }
    }

    public void add(Multimap<Class<?>, DefineBeanConfig> defineClassMultimap) {
        if (defineClassMultimap != null && defineClassMultimap.size() > 0) {
            beanDefineMap.putAll(defineClassMultimap);
        }
    }

    public void add(@NotNull Class<?> clz) {
        final BeanDefine define = clz.getAnnotation(BeanDefine.class);
        if (define != null) {
            beanDefineMap.put(clz, new DefineBeanConfig().setType(define.type())
                    .setBeanName(define.name()).setSrcLocation(define.src()));
        }
    }

    private void scanConfigBeanDefine(String[] basePackagesArray) {
        if (basePackagesArray == null || basePackagesArray.length == 0) {
            return;
        }
        try {
            for (String basePackage : basePackagesArray) {
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
                            if (metadataReader.getAnnotationMetadata().hasAnnotation(BeanDefine.class.getName())) {
                                final String className = metadataReader.getClassMetadata().getClassName();
                                final Class<?> beanClazz =
                                        Thread.currentThread().getContextClassLoader().loadClass(className);
                                final BeanDefine define = beanClazz.getAnnotation(BeanDefine.class);
                                beanDefineMap.put(beanClazz, new DefineBeanConfig().setType(define.type())
                                        .setBeanName(define.name()).setSrcLocation(define.src()));
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
    public void afterPropertiesSet() throws Exception {
        for (Class<?> clazz : beanDefineMap.keySet()) {
            for (DefineBeanConfig define : beanDefineMap.get(clazz)) {
                switch (define.getType()) {
                    case YAML:
                        try {
                            try (InputStream is = new ClassPathResource(define.getSrcLocation()).getInputStream()) {
                                List list = new Yaml().loadAs(is, ArrayList.class);
                                for (int i = 0; i < list.size(); i++) {
                                    Object bean = JSON.parseObject(JSON.toJSONString(list.get(i)), clazz);
                                    registerBean(clazz, bean, i, define);
                                }
                            }
                        } catch (ClassCastException e) {
                            try (InputStream is = new ClassPathResource(define.getSrcLocation()).getInputStream()) {
                                Object bean = new Yaml().loadAs(is, clazz);
                                registerBean(clazz, bean, null, define);
                            }
                        }
                        break;
                    case JSON:
                        try {
                            try (InputStream is = new ClassPathResource(define.getSrcLocation()).getInputStream()) {
                                List list = JSON.parseArray(
                                        IOUtils.readFromInputStream(is, Charset.forName("UTF-8"), false),
                                        clazz);
                                for (int i = 0; i < list.size(); i++) {
                                    registerBean(clazz, list.get(i), i, define);
                                }
                            }
                        } catch (JSONException e) {
                            try (InputStream is = new ClassPathResource(define.getSrcLocation()).getInputStream()) {
                                Object bean = JSON.parseObject(is, clazz);
                                registerBean(clazz, bean, null, define);
                            }
                        }
                        break;
                    default:
                        throw new FatalBeanException("Unknown Bean Define Type: " + define.getType());
                }

            }
        }
    }

    private void registerBean(Class<?> clazz, Object bean, Integer index, DefineBeanConfig define) {
        DefaultListableBeanFactory defaultListableBeanFactory =
                (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        String beanName = StringUtils.isNotBlank(define.getBeanName()) ? define.getBeanName()
                : clazz.getName() + "_" + (index == null ? "" : index + "_") + define.getSrcLocation();
        defaultListableBeanFactory.applyBeanPostProcessorsAfterInitialization(bean, beanName);
        defaultListableBeanFactory.registerSingleton(beanName, bean);
        log.info("BeanDefine definition: name={},type={},src={}", beanName, clazz.getName(), define.getSrcLocation());
        nameBeanMap.put(beanName, applicationContext.getBean(beanName));
    }

}
