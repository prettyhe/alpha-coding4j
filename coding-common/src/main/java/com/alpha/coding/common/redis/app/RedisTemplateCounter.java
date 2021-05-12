package com.alpha.coding.common.redis.app;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;

import com.alpha.coding.bo.function.Counter;

/**
 * RedisTemplateCounter
 *
 * @version 1.0
 * Date: 2021/5/12
 */
public class RedisTemplateCounter implements Counter {

    private final RedisTemplate redisTemplate;

    public RedisTemplateCounter(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Long obtain(String key) {
        final Object val = redisTemplate.opsForValue().get(key);
        return val == null ? null : Long.parseLong(String.valueOf(val));
    }

    @Override
    public Long incr(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    @Override
    public void expire(String key, long timeout, TimeUnit timeUnit) {
        redisTemplate.expire(key, timeout, timeUnit);
    }

}
