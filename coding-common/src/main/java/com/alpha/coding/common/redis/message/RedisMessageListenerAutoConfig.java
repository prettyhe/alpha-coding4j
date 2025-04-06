package com.alpha.coding.common.redis.message;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.alpha.coding.common.utils.ClassUtils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * RedisMessageListenerAutoConfig
 *
 * @version 1.0
 * Date: 2020-02-18
 */
@Data
@Slf4j
public class RedisMessageListenerAutoConfig implements ApplicationListener<ContextRefreshedEvent>, Ordered {

    private final Map<Class<? extends Supplier<RedisSerializer>>, RedisSerializer> redisSerializerMap = new HashMap<>();

    private List<Topic> resolveTopic(String[] strings, Function<String, Topic> stringTopicFunction) {
        return Arrays.stream(strings).map(stringTopicFunction).collect(Collectors.toList());
    }

    private void addListener(RedisMessage redisMessage, RedisMessageListenerContainer container,
                             MessageListener listener) {
        if (container == null) {
            return;
        }
        RedisSerializer redisKeySerializer = null;
        if (!redisMessage.redisKeySerializerSupplier().equals(NoneRedisSerializerProvider.class)) {
            redisKeySerializer = redisSerializerMap.computeIfAbsent(redisMessage.redisKeySerializerSupplier(), k -> {
                try {
                    return ClassUtils.newInstance(k).get();
                } catch (Exception e) {
                    log.warn("make instance for key Supplier<RedisSerializer> fail, class is {}",
                            redisMessage.redisKeySerializerSupplier().getName(), e);
                    throw new RuntimeException(e);
                }
            });
        }
        if (redisMessage.topic().length > 0) {
            if (redisKeySerializer != null) {
                container.setTopicSerializer(redisKeySerializer);
            }
            container.addMessageListener(listener, resolveTopic(redisMessage.topic(), ChannelTopic::of));
            log.info("Redis MessageListener listen ChannelTopic {} with {}",
                    redisMessage.topic(), listener.getClass().getName());
        }
        if (redisMessage.topicPattern().length > 0) {
            if (redisKeySerializer != null) {
                container.setTopicSerializer(redisKeySerializer);
            }
            container.addMessageListener(listener, resolveTopic(redisMessage.topicPattern(), PatternTopic::of));
            log.info("Redis MessageListener listen PatternTopic {} with {}",
                    redisMessage.topicPattern(), listener.getClass().getName());
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        final ApplicationContext applicationContext = contextRefreshedEvent.getApplicationContext();
        final Map<String, RedisMessageListenerContainer> containerMap =
                applicationContext.getBeansOfType(RedisMessageListenerContainer.class);
        final Map<String, MessageListener> listenerMap = applicationContext.getBeansOfType(MessageListener.class);
        listenerMap.values().stream()
                .filter(p -> p.getClass().isAnnotationPresent(RedisMessage.class))
                .forEach(listener -> {
                    final RedisMessage redisMessage = listener.getClass().getAnnotation(RedisMessage.class);
                    if (redisMessage.injectAllListenerContainer()) {
                        containerMap.forEach((x, y) -> addListener(redisMessage, y, listener));
                    } else {
                        Arrays.stream(redisMessage.listenerContainer())
                                .forEach(x -> addListener(redisMessage, containerMap.get(x), listener));
                    }
                });
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
