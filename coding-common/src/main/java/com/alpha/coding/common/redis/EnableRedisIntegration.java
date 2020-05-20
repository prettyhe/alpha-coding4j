package com.alpha.coding.common.redis;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.alpha.coding.common.bean.register.EnableAutoRegistrar;

/**
 * EnableRedisIntegration redis集成配置，所需redis配置参考RedisIntegrationConfiguration
 * <p>是否开启Redis缓存</p>
 * <p>是否开启RedisMessage</p>
 *
 * @version 1.0
 * Date: 2020-03-20
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@EnableAutoRegistrar
@Import(RedisIntegrationConfiguration.class)
public @interface EnableRedisIntegration {

    /**
     * 是否启用RedisCache
     */
    boolean enableRedisCache() default true;

    /**
     * RedisCache Key前缀，仅在启用RedisCache时生效
     */
    String redisCacheKeyPrefix() default "RE:CH";

    /**
     * RedisCache 缓存默认过期时间，仅在启用RedisCache时生效
     */
    long defaultRedisCacheExpire() default 60;

    /**
     * 本地缓存CacheManager的beanName，仅在启用RedisCache时生效
     */
    String localCacheManager() default "";

    /**
     * 是否启用RedisMessageListener
     */
    boolean enableRedisMessageListener() default false;

}
