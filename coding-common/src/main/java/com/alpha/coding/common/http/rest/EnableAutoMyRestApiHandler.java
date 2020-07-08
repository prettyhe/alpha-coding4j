package com.alpha.coding.common.http.rest;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.ClassUtils;

import com.alpha.coding.common.bean.comm.CustomFactoryBean;
import com.alpha.coding.common.bean.register.BeanDefineUtils;
import com.alpha.coding.common.bean.spi.ConfigurationRegisterHandler;
import com.alpha.coding.common.bean.spi.RegisterBeanDefinitionContext;
import com.alpha.coding.common.http.CachedHttpComponentsClientHttpRequestFactory;
import com.alpha.coding.common.utils.SpringAnnotationConfigUtils;
import com.alpha.coding.common.utils.StringUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

/**
 * EnableAutoMyRestApiHandler
 *
 * @version 1.0
 * Date: 2020/6/12
 */
@Slf4j
public class EnableAutoMyRestApiHandler implements ConfigurationRegisterHandler {

    private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    private MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory((ResourceLoader) null);

    @Override
    public void registerBeanDefinitions(RegisterBeanDefinitionContext context) {
        Set<AnnotationAttributes> annotationAttributes = SpringAnnotationConfigUtils.attributesForRepeatable(
                context.getImportingClassMetadata(), EnableAutoMyRestApis.class, EnableAutoMyRestApi.class);
        if (CollectionUtils.isEmpty(annotationAttributes)) {
            return;
        }
        final Environment environment = context.getEnvironment();
        final BeanDefinitionRegistry registry = context.getRegistry();
        for (AnnotationAttributes attributes : annotationAttributes) {
            final String restTemplateRef = attributes.getString("restTemplateRef");
            final String prefix = attributes.getString("prefix");
            if (StringUtils.isBlank(restTemplateRef)) {
                // 注册 PoolingHttpClientConnectionManager
                BeanDefinitionBuilder managerDefinitionBuilder =
                        BeanDefinitionBuilder.genericBeanDefinition(CustomFactoryBean.class);
                PoolingHttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager();
                poolingConnectionManager.setMaxTotal((Integer) BeanDefineUtils
                        .fetchProperty(environment, Arrays.asList(prefix + ".http.pool.conn.maxTotal",
                                "http.pool.conn.maxTotal"), Integer.class, 1000));
                poolingConnectionManager.setDefaultMaxPerRoute((Integer) BeanDefineUtils
                        .fetchProperty(environment, Arrays.asList(prefix + ".http.pool.conn.maxPerRoute",
                                "http.pool.conn.maxPerRoute"), Integer.class, 100));
                managerDefinitionBuilder.addPropertyValue("type", PoolingHttpClientConnectionManager.class);
                managerDefinitionBuilder.addPropertyValue("target", poolingConnectionManager);
                registry.registerBeanDefinition(prefix + "PoolingHttpClientConnectionManager",
                        managerDefinitionBuilder.getBeanDefinition());
                log.info("registerBeanDefinition {} for prefix {}",
                        prefix + "PoolingHttpClientConnectionManager", prefix);
                // 注册 ClientHttpRequestFactory
                final RequestConfig config = RequestConfig.custom()
                        .setConnectTimeout((Integer) BeanDefineUtils
                                .fetchProperty(environment, Arrays.asList(prefix + ".http.conn.timeout",
                                        "http.conn.timeout"), Integer.class, 3000))
                        .setConnectionRequestTimeout((Integer) BeanDefineUtils
                                .fetchProperty(environment, Arrays.asList(prefix + ".http.conn.timeout",
                                        "http.conn.timeout"), Integer.class, 3000))
                        .setSocketTimeout((Integer) BeanDefineUtils
                                .fetchProperty(environment, Arrays.asList(prefix + ".http.socket.timeout",
                                        "http.socket.timeout"), Integer.class, 3000))
                        .build();
                ClientHttpRequestFactory clientHttpRequestFactory = new CachedHttpComponentsClientHttpRequestFactory()
                        .setHttpClientConnectionManager(poolingConnectionManager)
                        .setRequestConfig(config);
                BeanDefinitionBuilder clientHttpRequestFactoryDefinitionBuilder =
                        BeanDefinitionBuilder.genericBeanDefinition(CustomFactoryBean.class);
                clientHttpRequestFactoryDefinitionBuilder.addPropertyValue("type", ClientHttpRequestFactory.class);
                clientHttpRequestFactoryDefinitionBuilder.addPropertyValue("target", clientHttpRequestFactory);
                registry.registerBeanDefinition(prefix + "ClientHttpRequestFactory",
                        clientHttpRequestFactoryDefinitionBuilder.getBeanDefinition());
                log.info("registerBeanDefinition {} for prefix {}",
                        prefix + "ClientHttpRequestFactory", prefix);
                // 注册 MyRestTemplate
                MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
                converter.setSupportedMediaTypes(Lists.newArrayList(MediaType.APPLICATION_JSON));
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                converter.setObjectMapper(objectMapper);
                MyRestTemplate restTemplate = new MyRestTemplate(clientHttpRequestFactory);
                restTemplate.setMessageConverters(Lists.newArrayList(converter));
                BeanDefinitionBuilder myRestTemplateDefinitionBuilder =
                        BeanDefinitionBuilder.genericBeanDefinition(CustomFactoryBean.class);
                myRestTemplateDefinitionBuilder.addPropertyValue("type", MyRestTemplate.class);
                myRestTemplateDefinitionBuilder.addPropertyValue("target", restTemplate);
                registry.registerBeanDefinition(prefix + "MyRestTemplate",
                        myRestTemplateDefinitionBuilder.getBeanDefinition());
                log.info("registerBeanDefinition {} for prefix {}",
                        prefix + "MyRestTemplate", prefix);
            }
            // 注册HttpAPIFactory
            final String httpAPIFactoryBeanName = attributes.getString("httpAPIFactoryBeanName");
            if (registry.containsBeanDefinition(httpAPIFactoryBeanName)) {
                log.warn("bean {} already exist and will override", httpAPIFactoryBeanName);
            }
            final String uri = (String) BeanDefineUtils.fetchProperty(environment,
                    Arrays.asList(prefix + ".remote.uri", "remote.uri"), String.class, "");
            final Map<Class, String> apis = new HashMap<>();
            for (Class<?> apiClass : attributes.getClassArray("apiClasses")) {
                apis.put(apiClass, uri);
            }
            final String[] basePackages = attributes.getStringArray("scanBasePackages");
            try {
                for (String basePackage : basePackages) {
                    if (StringUtils.isBlank(basePackage)) {
                        continue;
                    }
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
                                if (metadataReader.getClassMetadata().isInterface()) {
                                    final String className = metadataReader.getClassMetadata().getClassName();
                                    final Class<?> beanClazz =
                                            Thread.currentThread().getContextClassLoader().loadClass(className);
                                    apis.put(beanClazz, uri);
                                }
                            } catch (Throwable ex) {
                                throw new FatalBeanException("Failed to load class: " + resource, ex);
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
            // 先注册API，避免引用错误
            HttpAPIFactoryUtils.registerApi(registry, context.getBeanFactory(),
                    (MyRestTemplate) context.getBeanFactory().getBean(prefix + "MyRestTemplate"), apis, null);
            BeanDefinitionBuilder httpAPIFactoryDefinitionBuilder =
                    BeanDefinitionBuilder.genericBeanDefinition(HttpAPIFactory.class);
            httpAPIFactoryDefinitionBuilder.addPropertyValue("apis", apis);
            httpAPIFactoryDefinitionBuilder.addPropertyValue("restTemplate",
                    context.getBeanFactory().getBean(
                            StringUtils.isBlank(restTemplateRef) ? prefix + "MyRestTemplate" : restTemplateRef));
            registry.registerBeanDefinition(httpAPIFactoryBeanName,
                    httpAPIFactoryDefinitionBuilder.getBeanDefinition());
            log.info("registerBeanDefinition {} for prefix {}", httpAPIFactoryBeanName, prefix);
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
