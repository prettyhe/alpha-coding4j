package com.alpha.coding.common.redis;

import java.util.List;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

/**
 * RedisBean
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class RedisBean {

    @Setter
    private JedisPool pool;

    @Setter
    private int db;

    /**
     * 获取jedis客户端,注意使用后需要回收资源
     */
    public Jedis getClient() {
        Jedis jedis = pool.getResource();
        if (db != 0) {
            jedis.select(db);
        }
        return jedis;
    }

    /**
     * 回收redis资源
     *
     * @param jedis jedis客户端
     */
    public void returnSource(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

    /**
     * 使用回调接口执行redis操作，封装回收资源逻辑
     *
     * @param callback redis操作回调
     *
     * @return 返回操作结果
     */
    public <T> T doInRedis(JedisCallback<T> callback) {
        Jedis jedis = getClient();
        try {
            return callback.execute(jedis);
        } finally {
            jedis.close();
        }
    }

    /**
     * jedis操作回调接口
     */
    public static interface JedisCallback<T> {

        T execute(Jedis jedis);

    }

    /**
     * 使用回调接口执行redis管道操作，无返回结果，封装回收资源逻辑
     *
     * @param callback redis操作回调
     */
    public void pipeline(JedisPipelineCallback callback) {
        Jedis jedis = getClient();
        try {
            Pipeline pipelined = jedis.pipelined();
            try {
                callback.pipeline(pipelined);
            } finally {
                pipelined.sync();
            }
        } finally {
            jedis.close();
        }
    }

    /**
     * 使用回调接口执行redis管道操作，返回结果，封装回收资源逻辑
     *
     * @param callback redis操作回调
     *
     * @return 返回管道操作结果
     */
    public List<Object> pipelineWithReturn(JedisPipelineCallback callback) {
        Jedis jedis = getClient();
        try {
            Pipeline pipelined = jedis.pipelined();
            try {
                callback.pipeline(pipelined);
            } catch (Exception e) {
                pipelined.sync();
                throw e;
            }
            return pipelined.syncAndReturnAll();
        } finally {
            jedis.close();
        }
    }

    /**
     * jedis管道操作回调接口
     */
    public static interface JedisPipelineCallback {

        void pipeline(Pipeline pipelined);

    }

    /**
     * redis操作异常
     */
    public static class RedisOperateException extends RuntimeException {

        private static final long serialVersionUID = 4390267209932448524L;

        public RedisOperateException(Exception e) {
            super(e);
        }

    }

}
