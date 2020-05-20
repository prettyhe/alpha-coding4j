package com.alpha.coding.common.redis.cache.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RedisCacheEvict
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface RedisCacheEvict {

    /**
     * redisTemplate的bean名称
     */
    String redisBean() default "";

    /**
     * 缓存名称
     */
    String name() default "";

    /**
     * 缓存key，SpEL表达式
     */
    String key() default "";

    /**
     * 本地缓存名，非空时启用本地缓存
     */
    String localName() default "";

    /**
     * 本地缓存管理器，非空时使用指定的缓存管理器
     */
    String localCacheManager() default "";

}
