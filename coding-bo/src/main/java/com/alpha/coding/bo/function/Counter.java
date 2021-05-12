package com.alpha.coding.bo.function;

import java.util.concurrent.TimeUnit;

/**
 * Counter
 *
 * @version 1.0
 * Date: 2021/5/12
 */
public interface Counter {

    /**
     * 获取当前计数值
     *
     * @param key key
     * @return 计数值
     */
    Long obtain(String key);

    /**
     * 自增
     *
     * @param key   key
     * @param delta 增量
     * @return 自增之后的值
     */
    Long incr(String key, long delta);

    /**
     * 设置有效期
     *
     * @param key     key
     * @param timeout 时长
     * @param unit    单位
     */
    void expire(String key, long timeout, TimeUnit unit);

}
