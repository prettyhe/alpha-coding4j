package com.alpha.coding.common.redis.message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.alibaba.fastjson.JSON;
import com.alpha.coding.common.utils.ClassUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * RedisMessagePublisherTemplate
 *
 * @version 1.0
 * Date: 2020-02-17
 */
@Slf4j
public abstract class RedisMessagePublisherTemplate implements RedisMessagePublisher {

    @Getter
    @Setter
    private List<String> topics;
    @Getter
    @Setter
    private RedisSerializer redisKeySerializer;
    @Getter
    @Setter
    private RedisSerializer redisValueSerializer;

    private volatile boolean initialized;

    /**
     * RedisTemplate钩子方法
     */
    protected abstract RedisTemplate getRedisTemplate();

    @Override
    public void sendMsg(Object message) {
        if (log.isDebugEnabled()) {
            log.debug("publish redis Message: {}", (message instanceof String) ? message : JSON.toJSONString(message));
        }
        if (!initialized) {
            parseConfig();
        }
        if (topics == null || topics.isEmpty()) {
            return;
        }
        final RedisTemplate redisTemplate = getRedisTemplate();
        final List<byte[]> rawTopics = new ArrayList<>(topics.size());
        final RedisSerializer redisKeySerializer = Optional.ofNullable(this.redisKeySerializer)
                .orElseGet(redisTemplate::getKeySerializer);
        for (String topic : topics) {
            rawTopics.add(redisKeySerializer.serialize(topic));
        }
        final RedisSerializer redisValueSerializer = Optional.ofNullable(this.redisValueSerializer)
                .orElseGet(redisTemplate::getValueSerializer);
        final byte[] rawValue = redisValueSerializer.serialize(message);
        redisTemplate.executePipelined((RedisCallback<Object>) redisConnection -> {
            for (byte[] topic : rawTopics) {
                redisConnection.publish(topic, rawValue);
            }
            return null;
        });
    }

    private synchronized void parseConfig() {
        try {
            if (!this.getClass().isAnnotationPresent(RedisMessage.class) || initialized) {
                return;
            }
            final RedisMessage redisMessage = this.getClass().getAnnotation(RedisMessage.class);
            if (this.topics != null) {
                for (String topic : redisMessage.topic()) {
                    if (!this.topics.contains(topic)) {
                        this.topics.add(topic);
                    }
                }
            } else {
                this.topics = new ArrayList<>(Arrays.asList(redisMessage.topic()));
            }
            if (!redisMessage.redisKeySerializerSupplier().equals(NoneRedisSerializerProvider.class)) {
                try {
                    redisKeySerializer = ClassUtils.newInstance(redisMessage.redisKeySerializerSupplier()).get();
                } catch (Exception e) {
                    log.warn("make instance for key Supplier<RedisSerializer> fail, class is {}",
                            redisMessage.redisKeySerializerSupplier().getName(), e);
                    throw new RuntimeException(e);
                }
            }
            if (!redisMessage.redisValueSerializerSupplier().equals(NoneRedisSerializerProvider.class)) {
                try {
                    redisValueSerializer = ClassUtils.newInstance(redisMessage.redisValueSerializerSupplier()).get();
                } catch (Exception e) {
                    log.warn("make instance for value Supplier<RedisSerializer> fail, class is {}",
                            redisMessage.redisValueSerializerSupplier().getName(), e);
                    throw new RuntimeException(e);
                }
            }
        } finally {
            initialized = true;
        }
    }

}
