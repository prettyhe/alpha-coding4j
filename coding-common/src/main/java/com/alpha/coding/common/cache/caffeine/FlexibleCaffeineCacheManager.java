package com.alpha.coding.common.cache.caffeine;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * FlexibleCaffeineCacheManager
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class FlexibleCaffeineCacheManager implements CacheManager, InitializingBean, ApplicationContextAware {

    @Setter
    private ApplicationContext applicationContext;

    private Map<String, CaffeineConfig> cacheConfigMap = new ConcurrentHashMap<>();
    private Map<String, CaffeineCacheManager> cacheManagerMap = new ConcurrentHashMap<>();

    public FlexibleCaffeineCacheManager() {
    }

    public FlexibleCaffeineCacheManager(Map<String, CaffeineConfig> cacheConfigMap) {
        if (cacheConfigMap != null) {
            this.cacheConfigMap.putAll(cacheConfigMap);
        }
    }

    public FlexibleCaffeineCacheManager(Collection<CaffeineConfig> configs) {
        if (configs != null) {
            for (CaffeineConfig config : configs) {
                cacheConfigMap.put(config.getCacheName(), config);
            }
        }
    }

    public void addCache(String configName, CaffeineConfig config) {
        if (config != null) {
            CaffeineCacheManager cacheManager = new CaffeineCacheManager(config.getCacheName());
            cacheManager.setAllowNullValues(config.isAllowNullValues());
            if (config.getLoaderFunction() != null) {
                cacheManager.setCacheLoader(config.getLoaderFunction().apply(applicationContext,
                        config.getLoaderName()));
            }
            cacheManager.setCacheSpecification(config.getSpec());
            cacheManagerMap.put(config.getCacheName(), cacheManager);
            cacheConfigMap.put(config.getCacheName(), config);
            log.info("load Caffeine from {} for name={},loader={},spec={}",
                    configName, config.getCacheName(), config.getLoaderName(), config.getSpec());
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final Map<String, CaffeineConfig> beansOfType = applicationContext.getBeansOfType(CaffeineConfig.class);
        if (beansOfType != null) {
            for (Map.Entry<String, CaffeineConfig> entry : beansOfType.entrySet()) {
                addCache(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public Cache getCache(String name) {
        return cacheManagerMap.get(name).getCache(name);
    }

    @Override
    public Collection<String> getCacheNames() {
        return cacheManagerMap.keySet();
    }
}
