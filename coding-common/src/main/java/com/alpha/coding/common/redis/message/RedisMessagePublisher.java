package com.alpha.coding.common.redis.message;

/**
 * RedisMessagePublisher
 *
 * @version 1.0
 * Date: 2020-02-17
 */
public interface RedisMessagePublisher {

    void sendMsg(Object message);

}
