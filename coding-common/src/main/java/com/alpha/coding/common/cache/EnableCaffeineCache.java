package com.alpha.coding.common.cache;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * EnableCaffeineCache 开启基于Caffeine的本地缓存
 * <p>classpath下放置caffeine.yml的配置文件，格式如下</p>
 * <p>{cacheName: s30, spec: "initialCapacity=100,maximumSize=2000,expireAfterWrite=30s"}</p>
 *
 * @version 1.0
 * Date: 2020-03-20
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(CaffeineCacheConfiguration.class)
public @interface EnableCaffeineCache {
}
