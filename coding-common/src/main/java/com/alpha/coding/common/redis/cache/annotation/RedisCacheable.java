package com.alpha.coding.common.redis.cache.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.alpha.coding.common.redis.cache.ExpireStrategy;

/**
 * RedisCacheable
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface RedisCacheable {

    /**
     * redisTemplate的bean名称
     */
    String redisBean() default "";

    /**
     * 过期时间(秒),默认60秒
     */
    long expire() default 60;

    /**
     * 缓存名称
     */
    String name() default "";

    /**
     * 缓存key，SpEL表达式
     */
    String key() default "";

    /**
     * 是否使用gzip压缩
     */
    boolean gzip() default false;

    /**
     * 是否缓存null数据
     */
    boolean cacheNull() default false;

    /**
     * 同步加载缓存数据
     */
    boolean syncLoad() default false;

    /**
     * 缓存时间策略实现类
     */
    Class<? extends ExpireStrategy> expireStrategy() default ExpireStrategy.class;

    /**
     * 本地缓存名，非空时启用本地缓存
     */
    String localName() default "";

    /**
     * 本地缓存管理器，非空时使用指定的缓存管理器
     */
    String localCacheManager() default "";

}
