package com.alpha.coding.common.redis.message;

import java.util.function.Supplier;

import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * NoneRedisSerializerProvider
 *
 * @version 1.0
 * Date: 2020-02-17
 */
public class NoneRedisSerializerProvider implements Supplier<RedisSerializer> {

    @Override
    public RedisSerializer get() {
        return null;
    }
}
