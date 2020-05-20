package com.alpha.coding.common.redis.message;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
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
        if (CollectionUtils.isEmpty(topics)) {
            return;
        }
        final List<byte[]> rawTopics = topics.stream()
                .map(p -> getRedisTemplate().getKeySerializer().serialize(p))
                .collect(Collectors.toList());
        byte[] rawValue = redisSerializer == null ? getRedisTemplate().getValueSerializer().serialize(message) :
                redisSerializer.serialize(message);
        rawTopics.forEach(t -> getRedisTemplate().execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                redisConnection.publish(t, rawValue);
                return null;
            }
        }));
    }

    private void parseConfig() {
        try {
            if (!this.getClass().isAnnotationPresent(RedisMessage.class)) {
                return;
            }
            final RedisMessage redisMessage = this.getClass().getAnnotation(RedisMessage.class);
            if (redisMessage.topic() != null) {
                if (this.topics != null) {
                    this.topics.addAll(Arrays.asList(redisMessage.topic()));
                } else {
                    this.topics = Arrays.asList(redisMessage.topic());
                }
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
