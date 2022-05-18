package com.alpha.coding.common.redis;

import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.cache.CacheManager;
import org.springframework.core.annotation.AnnotationAttributes;

import com.alpha.coding.common.aop.AopBeanDefinitionRegistry;
import com.alpha.coding.common.aop.AspectJParams;
import com.alpha.coding.common.bean.spi.ConfigurationRegisterHandler;
import com.alpha.coding.common.bean.spi.RegisterBeanDefinitionContext;
import com.alpha.coding.common.executor.MDCThreadPoolTaskExecutor;
import com.alpha.coding.common.redis.cache.CacheConfig;
import com.alpha.coding.common.redis.cache.RedisCacheAspect;
import com.alpha.coding.common.redis.cache.annotation.RedisCacheEvict;
import com.alpha.coding.common.redis.cache.annotation.RedisCachePut;
import com.alpha.coding.common.redis.cache.annotation.RedisCacheable;
import com.alpha.coding.common.redis.message.RedisMessageListenerAutoConfig;
import com.alpha.coding.common.utils.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * EnableRedisIntegrationHandler
 *
 * @version 1.0
 * Date: 2020-03-20
 */
@Slf4j
public class EnableRedisIntegrationHandler implements ConfigurationRegisterHandler {

    @Override
    public void registerBeanDefinitions(RegisterBeanDefinitionContext context) {
        AnnotationAttributes attributes = AnnotationAttributes
                .fromMap(context.getImportingClassMetadata()
                        .getAnnotationAttributes(EnableRedisIntegration.class.getName()));
        if (attributes == null || attributes.isEmpty()) {
            return;
        }
        handleRedisCache(attributes, context);
        handleRedisMessageListener(attributes, context);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private void handleRedisCache(AnnotationAttributes attributes, RegisterBeanDefinitionContext context) {
        if (!attributes.getBoolean("enableRedisCache")) {
            return;
        }
        // 注册CacheConfig
        BeanDefinitionBuilder cacheConfigDefinitionBuilder =
                BeanDefinitionBuilder.genericBeanDefinition(CacheConfig.class);
        cacheConfigDefinitionBuilder.addPropertyValue("redisTemplateName", "stringRedisTemplate");
        cacheConfigDefinitionBuilder.addPropertyValue("expire",
                attributes.getNumber("defaultRedisCacheExpire").longValue());
        cacheConfigDefinitionBuilder.addPropertyValue("name",
                attributes.getString("redisCacheKeyPrefix"));
        context.getRegistry().registerBeanDefinition("defaultRedisCacheConfig",
                cacheConfigDefinitionBuilder.getBeanDefinition());
        // 注册RedisCacheAspect
        BeanDefinitionBuilder annotationRedisCacheAspectBuilder =
                BeanDefinitionBuilder.genericBeanDefinition(RedisCacheAspect.class);
        annotationRedisCacheAspectBuilder.addPropertyReference("cacheConfig", "defaultRedisCacheConfig");
        if (StringUtils.isNotBlank(attributes.getString("localCacheManager"))) {
            annotationRedisCacheAspectBuilder.addPropertyReference("localCacheManager",
                    attributes.getString("localCacheManager"));
        } else {
            try {
                final CacheManager cacheManager = context.getBeanFactory().getBean(CacheManager.class);
                annotationRedisCacheAspectBuilder.addPropertyValue("localCacheManager", cacheManager);
            } catch (BeansException e) {
                log.warn("can not wire CacheManager for localCache");
            }
        }
        context.getRegistry().registerBeanDefinition("annotationRedisCacheAspect",
                annotationRedisCacheAspectBuilder.getBeanDefinition());
        // 注册AOP
        String pointcutTemplate = "@within(%s)\n"
                + "                        || @annotation(%s)\n"
                + "                        || @within(%s)\n"
                + "                        || @annotation(%s)\n"
                + "                        || @within(%s)\n"
                + "                        || @annotation(%s)";
        final AspectJParams params = new AspectJParams()
                .setProxyTargetClass(true)
                .setAopRefBean("annotationRedisCacheAspect")
                .setAopOrder(1)
                .setAopAdvice("around")
                .setAopAdviceMethod("doCacheAspect")
                .setAopPointcut(String.format(pointcutTemplate,
                        RedisCacheable.class.getName(), RedisCacheable.class.getName(),
                        RedisCachePut.class.getName(), RedisCachePut.class.getName(),
                        RedisCacheEvict.class.getName(), RedisCacheEvict.class.getName()));
        try {
            AopBeanDefinitionRegistry.loadBeanDefinitions(context.getRegistry(), params);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleRedisMessageListener(AnnotationAttributes attributes, RegisterBeanDefinitionContext context) {
        if (!attributes.getBoolean("enableRedisMessageListener")) {
            return;
        }
        // 注册RedisMessageListenerContainer
        BeanDefinitionBuilder containerDefinitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition("org.springframework.data.redis.listener.RedisMessageListenerContainer");
        containerDefinitionBuilder.addPropertyReference("connectionFactory", "defaultJedisConnectionFactory");
        final int availableProcessors = Runtime.getRuntime().availableProcessors();
        containerDefinitionBuilder.addPropertyValue("taskExecutor",
                MDCThreadPoolTaskExecutor.builder()
                        .corePoolSize(availableProcessors * 2)
                        .maxPoolSize(availableProcessors * 2)
                        .keepAliveSeconds(300)
                        .namedThreadFactory("RedisMsgTask")
                        .queueCapacity(1000)
                        .rejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy())
                        .build());
        containerDefinitionBuilder.setScope(BeanDefinition.SCOPE_SINGLETON);
        context.getRegistry().registerBeanDefinition("redisMessageListenerContainer",
                containerDefinitionBuilder.getBeanDefinition());
        // 注册RedisMessageListenerAutoConfig
        BeanDefinitionBuilder autoConfigDefinitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(RedisMessageListenerAutoConfig.class);
        autoConfigDefinitionBuilder.setScope(BeanDefinition.SCOPE_SINGLETON);
        context.getRegistry().registerBeanDefinition("redisMessageListenerAutoConfig",
                autoConfigDefinitionBuilder.getBeanDefinition());
    }

}
