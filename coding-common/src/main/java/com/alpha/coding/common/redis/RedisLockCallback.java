package com.alpha.coding.common.redis;

/**
 * RedisLockCallback
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public interface RedisLockCallback<T> {

    T execute();

}
