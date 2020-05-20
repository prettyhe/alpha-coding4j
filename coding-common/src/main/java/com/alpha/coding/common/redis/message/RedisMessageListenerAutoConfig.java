package com.alpha.coding.common.redis.message;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import com.alpha.coding.bo.function.TiConsumer;

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
public class RedisMessageListenerAutoConfig implements ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        final Map<String, RedisMessageListenerContainer> containerMap =
                applicationContext.getBeansOfType(RedisMessageListenerContainer.class);
        final Map<String, MessageListener> listenerMap = applicationContext.getBeansOfType(MessageListener.class);
        final TiConsumer<RedisMessage, MessageListener, RedisMessageListenerContainer> injectConsumer =
                (an, listener, container) -> {
                    if (container == null || listener == null) {
                        return;
                    }
                    if (an.topic() != null && an.topic().length > 0) {
                        container.addMessageListener(listener, Arrays.stream(an.topic())
                                .map(x -> new ChannelTopic(x)).collect(Collectors.toList()));
                    }
                    if (an.topicPattern() != null && an.topicPattern().length > 0) {
                        container.addMessageListener(listener, Arrays.stream(an.topicPattern())
                                .map(x -> new PatternTopic(x)).collect(Collectors.toList()));
                    }
                };
        listenerMap.forEach((k, v) -> {
            if (v.getClass().isAnnotationPresent(RedisMessage.class)) {
                final RedisMessage redisMessage = v.getClass().getAnnotation(RedisMessage.class);
                if (redisMessage.injectAllListenerContainer()) {
                    containerMap.forEach((ck, cv) -> injectConsumer.accept(redisMessage, v, cv));
                } else {
                    Arrays.stream(redisMessage.listenerContainer())
                            .map(c -> containerMap.get(c)).forEach(c -> injectConsumer.accept(redisMessage, v, c));
                }
            }
        });
    }
}
