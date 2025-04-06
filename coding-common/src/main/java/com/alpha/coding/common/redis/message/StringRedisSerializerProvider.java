package com.alpha.coding.common.redis.message;

import java.util.function.Supplier;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * StringRedisSerializerProvider
 *
 * @version 1.0
 * Date: 2020-02-17
 */
public class StringRedisSerializerProvider implements Supplier<RedisSerializer> {

    @Override
    public RedisSerializer get() {
        return StringRedisSerializer.UTF_8;
    }

}
