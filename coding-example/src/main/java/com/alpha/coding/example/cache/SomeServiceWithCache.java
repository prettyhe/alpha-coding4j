package com.alpha.coding.example.cache;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.alpha.coding.common.redis.cache.ExpireStrategy;
import com.alpha.coding.common.redis.cache.annotation.RedisCacheEvict;
import com.alpha.coding.common.redis.cache.annotation.RedisCacheable;

/**
 * SomeServiceWithCache
 *
 * @version 1.0
 * Date: 2020-03-22
 */
@Component
public class SomeServiceWithCache implements ExpireStrategy {

    @RedisCacheable(key = "'redis_cache_example'", syncLoad = true, expireStrategy = SomeServiceWithCache.class)
    public List<Map<String, List<Integer>>> doSomeWithCache() {
        // TODO load data
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Arrays.asList(Collections.singletonMap("test", Arrays.asList(1)));
    }

    @RedisCacheEvict(key = "'data_a'")
    public void evictDataACache() {
        // nothing
    }

    @Override
    public long calculateExpire(Object[] args, Object returnValue) {
        return returnValue == null ? 5 : 60;
    }
}
