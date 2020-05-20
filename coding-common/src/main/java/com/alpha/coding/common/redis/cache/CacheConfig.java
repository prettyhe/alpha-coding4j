package com.alpha.coding.common.redis.cache;

import org.springframework.data.redis.core.RedisTemplate;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * CacheConfig
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Data
@Accessors(chain = true)
public class CacheConfig {

    private String redisTemplateName;
    private transient RedisTemplate redisTemplate;
    private long expire = 60; // 过期时间(秒)
    private String name; // redis缓存名
    private String key;
    private boolean gzip;
    private boolean cacheNull;
    private boolean syncLoad;
    private transient ExpireStrategy expireStrategy;
    private String localName; // 本地缓存名
    private String localCacheManager; // 本地缓存CacheManager
}
