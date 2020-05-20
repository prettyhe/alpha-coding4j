package com.alpha.coding.common.redis.cache;

/**
 * ExpireStrategy
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public interface ExpireStrategy {

    /**
     * 计算缓存时间
     *
     * @param args        参数
     * @param returnValue 返回值
     */
    long calculateExpire(Object[] args, Object returnValue);

}
