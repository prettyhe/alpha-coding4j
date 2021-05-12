package com.alpha.coding.common.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import com.alpha.coding.common.bean.define.DefineBeanConfig;
import com.alpha.coding.common.bean.define.DefineBeanFactory;
import com.alpha.coding.common.bean.define.DefineType;
import com.alpha.coding.common.cache.caffeine.CaffeineConfig;
import com.alpha.coding.common.cache.caffeine.FlexibleCaffeineCacheManager;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * CaffeineCacheConfiguration
 *
 * @version 1.0
 * Date: 2020-03-20
 */
@Configuration
@EnableCaching(proxyTargetClass = true)
public class CaffeineCacheConfiguration {

    @Bean("cacheBeanDefineBeanFactory")
    public static DefineBeanFactory defineBeanFactory() {
        Multimap<Class<?>, DefineBeanConfig> defineClassMultimap = ArrayListMultimap.create();
        defineClassMultimap.put(CaffeineConfig.class, new DefineBeanConfig()
                .setType(DefineType.YAML).setSrcLocation("caffeine.yml"));
        return new DefineBeanFactory(defineClassMultimap);
    }

    @Bean("caffeineCacheManager")
    @DependsOn("cacheBeanDefineBeanFactory")
    public static CacheManager flexibleCaffeineCacheManager() {
        return new FlexibleCaffeineCacheManager();
    }

}
