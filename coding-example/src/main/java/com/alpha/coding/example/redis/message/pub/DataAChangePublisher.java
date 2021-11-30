package com.alpha.coding.example.redis.message.pub;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import com.alpha.coding.common.redis.message.RedisMessage;
import com.alpha.coding.common.redis.message.RedisMessagePublisherTemplate;
import com.alpha.coding.example.constant.RedisTopics;

import lombok.extern.slf4j.Slf4j;

/**
 * DataAChangePublisher
 *
 * @version 1.0
 * Date: 2020-02-19
 */
@Slf4j
@Component
@RedisMessage(topic = RedisTopics.DATA_A_CHANGE, redisSerializerSupplier = DataAChangePublisher.class)
public class DataAChangePublisher extends RedisMessagePublisherTemplate implements Supplier<RedisSerializer> {

    @Autowired
    @Qualifier("stringRedisTemplate")
    private RedisTemplate redisTemplate;

    private final FastJsonRedisSerializer fastJsonRedisSerializer = new FastJsonRedisSerializer(Object.class);

    @Override
    protected RedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    @Override
    public RedisSerializer get() {
        return fastJsonRedisSerializer;
    }
}
