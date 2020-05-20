package com.alpha.coding.common.cache.caffeine;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheResolver;
import org.springframework.cache.interceptor.SimpleKeyGenerator;

import lombok.Setter;

/**
 * CaffeineCachingConfigurerSupport
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class CaffeineCachingConfigurerSupport extends CachingConfigurerSupport {

    @Setter
    private CacheManager cacheManager;
    @Setter
    private KeyGenerator keyGenerator = new SimpleKeyGenerator();
    @Setter
    private CacheErrorHandler errorHandler = new SimpleCacheErrorHandler();

    public CaffeineCachingConfigurerSupport() {
    }

    public CaffeineCachingConfigurerSupport(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public CacheManager cacheManager() {
        return cacheManager;
    }

    @Override
    public CacheResolver cacheResolver() {
        return new SimpleCacheResolver(cacheManager);
    }

    @Override
    public KeyGenerator keyGenerator() {
        return keyGenerator;
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return errorHandler;
    }
}
