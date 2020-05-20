package com.alpha.coding.example.redis.message.sub;

import java.nio.charset.Charset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import com.alpha.coding.common.redis.message.RedisMessage;
import com.alpha.coding.example.cache.SomeServiceWithCache;
import com.alpha.coding.example.constant.RedisTopics;

import lombok.extern.slf4j.Slf4j;

/**
 * LoanStatusListener
 *
 * @version 1.0
 * Date: 2020-02-19
 */
@Slf4j
@Component
@RedisMessage(topic = RedisTopics.DATA_A_CHANGE)
public class DataAChangeListener implements MessageListener {

    private final Charset charset = Charset.forName("UTF-8");

    @Autowired
    private SomeServiceWithCache someServiceWithCache;

    @Override
    public void onMessage(Message message, byte[] bytes) {
        String topic = new String(message.getChannel(), charset);
        String msg = new String(message.getBody(), charset);
        log.info("receive message: topic={}, msg={}", topic, msg);
        someServiceWithCache.evictDataACache();
    }
}
