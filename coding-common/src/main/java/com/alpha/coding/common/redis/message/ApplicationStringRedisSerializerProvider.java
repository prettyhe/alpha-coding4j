package com.alpha.coding.common.redis.message;

import java.util.function.Supplier;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.alpha.coding.common.bean.comm.ApplicationContextHolder;

/**
 * ApplicationStringRedisSerializerProvider
 *
 * @version 1.0
 * Date: 2020-02-17
 */
public class ApplicationStringRedisSerializerProvider implements Supplier<RedisSerializer> {

    @Override
    public RedisSerializer get() {
        try {
            return (StringRedisSerializer) ApplicationContextHolder.getBeanByName("stringRedisSerializer");
        } catch (Exception e) {
            try {
                return ApplicationContextHolder.getBeanByType(StringRedisSerializer.class);
            } catch (Exception e1) {
                return StringRedisSerializer.UTF_8;
            }
        }
    }
}
