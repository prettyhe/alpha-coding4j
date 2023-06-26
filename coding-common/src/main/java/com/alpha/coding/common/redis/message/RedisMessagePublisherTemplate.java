package com.alpha.coding.common.redis.message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.alibaba.fastjson.JSON;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * RedisMessagePublisherTemplate
 *
 * @version 1.0
 * Date: 2020-02-17
 */
@Slf4j
@Data
public abstract class RedisMessagePublisherTemplate implements RedisMessagePublisher, InitializingBean {

    private List<String> topics;
    private RedisSerializer redisSerializer;
    private boolean initialized;

    /**
     * RedisTemplate钩子方法
     */
    protected abstract RedisTemplate getRedisTemplate();

    @Override
    public void sendMsg(Object message) {
        if (log.isDebugEnabled()) {
            log.debug("publish redis Message: {}", JSON.toJSONString(message));
        }
        if (!initialized) {
            parseConfig();
        }
        if (topics == null || topics.isEmpty()) {
            return;
        }
        final List<byte[]> rawTopics = new ArrayList<>(topics.size());
        for (String topic : topics) {
            rawTopics.add(getRedisTemplate().getKeySerializer().serialize(topic));
        }
        final byte[] rawValue = redisSerializer == null ? getRedisTemplate().getValueSerializer()
                .serialize(message) : redisSerializer.serialize(message);
        getRedisTemplate().executePipelined((RedisCallback<Object>) redisConnection -> {
            for (byte[] topic : rawTopics) {
                redisConnection.publish(topic, rawValue);
            }
            return null;
        });
    }

    private void parseConfig() {
        try {
            if (!this.getClass().isAnnotationPresent(RedisMessage.class)) {
                return;
            }
            final RedisMessage redisMessage = this.getClass().getAnnotation(RedisMessage.class);
            if (this.topics != null) {
                this.topics.addAll(Arrays.asList(redisMessage.topic()));
            } else {
                this.topics = Arrays.asList(redisMessage.topic());
            }
            if (!redisMessage.redisSerializerSupplier().equals(NoneRedisSerializerProvider.class)) {
                try {
                    redisSerializer = redisMessage.redisSerializerSupplier().newInstance().get();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        } finally {
            initialized = true;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        parseConfig();
    }
}
