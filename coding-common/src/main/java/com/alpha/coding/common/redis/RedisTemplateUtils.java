package com.alpha.coding.common.redis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import com.alpha.coding.bo.base.Tuple;
import com.alpha.coding.bo.executor.SelfRefTimerTask;

import lombok.extern.slf4j.Slf4j;

/**
 * RedisTemplateUtils
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class RedisTemplateUtils {

    private static final String LOCK_SUCCESS = "OK";
    private static final String UNLOCK_SUCCESS = "1";
    private static final Timer LOCK_RENEWAL_TIMER = new Timer("RedisLockRenewalTimer", true);
    private static final Timer RENEWAL_CLEAR_TIMER = new Timer("RedisLockRenewalClearTimer", true);
    private static final AtomicLong TASK_CNT = new AtomicLong(0L);
    private static final Map<Long, TimerTask> CANCEL_TASK_MAP = new ConcurrentHashMap<>();

    static {
        RENEWAL_CLEAR_TIMER.scheduleAtFixedRate(new SelfRefTimerTask(t -> {
            List<Long> taskIds = new ArrayList<>();
            taskIds.addAll(CANCEL_TASK_MAP.keySet());
            if (taskIds.size() > 0) {
                for (Long taskId : taskIds) {
                    try {
                        CANCEL_TASK_MAP.get(taskId).cancel();
                    } catch (Exception e) {
                        //
                    }
                    CANCEL_TASK_MAP.remove(taskId);
                }
            }
            LOCK_RENEWAL_TIMER.purge();
        }), 10, 60000); // 每分钟清理一次
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOCK_RENEWAL_TIMER.cancel();
            RENEWAL_CLEAR_TIMER.cancel();
        }));
    }

    /**
     * 管道操作，拿到原始byte[]数据，需要自己实现反序列化
     */
    public static List<Object> executePipelined(final RedisTemplate redisTemplate, final RedisCallback<?> action) {
        return (List<Object>) redisTemplate.execute(new RedisCallback<List<Object>>() {
            public List<Object> doInRedis(RedisConnection connection) throws DataAccessException {
                connection.openPipeline();
                boolean pipelinedClosed = false;
                try {
                    Object result = action.doInRedis(connection);
                    if (result != null) {
                        throw new InvalidDataAccessApiUsageException(
                                "Callback cannot return a non-null value as it gets overwritten by the pipeline");
                    }
                    List<Object> closePipeline = connection.closePipeline();
                    pipelinedClosed = true;
                    // return deserializeMixedResults(closePipeline, resultSerializer, hashKeySerializer,
                    // hashValueSerializer);
                    return closePipeline;
                } finally {
                    if (!pipelinedClosed) {
                        connection.closePipeline();
                    }
                }
            }
        });
    }

    /**
     * 加锁操作
     *
     * @param redisTemplate redisTemplate
     * @param key           锁定的key
     * @param uqVal         获取锁客户端的位移标识
     * @param expireSeconds 锁定时长
     * @return 是否获取锁
     */
    public static boolean lock(final RedisTemplate redisTemplate, final String key,
                               final String uqVal, final long expireSeconds) {
        DefaultRedisScript<String> script = RedisScriptGenerator.generator("set-nx-ex.lua", String.class);
        final Object ret = redisTemplate.execute(script, Collections.singletonList(key),
                new Object[] {uqVal, String.valueOf(expireSeconds)});
        return ret == null ? false : LOCK_SUCCESS.equals(String.valueOf(ret));
    }

    /**
     * 解锁操作
     *
     * @param redisTemplate redisTemplate
     * @param key           锁定的key
     * @param uqVal         获取锁客户端的位移标识
     * @return 是否释放锁
     */
    public static boolean unlock(RedisTemplate redisTemplate, String key, String uqVal) {
        DefaultRedisScript<Long> script = RedisScriptGenerator.generator("del-by-val.lua", Long.class);
        final Object ret = redisTemplate
                .execute(script, Collections.singletonList(key), new Object[] {uqVal});
        return ret == null ? false : UNLOCK_SUCCESS.equals(String.valueOf(ret));
    }

    /**
     * 在锁中执行，获取不到锁时立即返回
     *
     * @param redisTemplate RedisTemplate
     * @param key           锁定的key
     * @param expireSeconds 锁定时间
     * @param callback      锁定期间回调
     * @return 执行结果元组(是否成功获取锁, 回调执行结果)
     */
    public static <T> Tuple<Boolean, T> doInLockReturnOnLockFail(RedisTemplate redisTemplate, String key,
                                                                 long expireSeconds, RedisLockCallback<T> callback) {
        final String valueId = System.currentTimeMillis() + "-"
                + UUID.randomUUID().toString().replaceAll("-", "")
                + "-" + expireSeconds * 1000;
        if (!lock(redisTemplate, key, valueId, expireSeconds)) {
            return new Tuple<>(false, null);
        }
        try {
            return new Tuple<>(true, callback.execute());
        } finally {
            unlock(redisTemplate, key, valueId);
        }
    }

    /**
     * 在锁中执行(自动续期)，获取不到锁时立即返回
     *
     * @param redisTemplate RedisTemplate
     * @param key           锁定的key
     * @param expireSeconds 锁定时间
     * @param callback      锁定期间回调
     * @return 执行结果元组(是否成功获取锁, 回调执行结果)
     */
    public static <T> Tuple<Boolean, T> doInLockAutoRenewalReturnOnLockFail(RedisTemplate redisTemplate, String key,
                                                                            long expireSeconds,
                                                                            RedisLockCallback<T> callback) {
        final String valueId = System.currentTimeMillis() + "-"
                + UUID.randomUUID().toString().replaceAll("-", "")
                + "-" + expireSeconds * 1000;
        if (!lock(redisTemplate, key, valueId, expireSeconds)) {
            return new Tuple<>(false, null);
        }
        final AtomicBoolean run = new AtomicBoolean(true);
        try {
            autoRenewal(redisTemplate, key, valueId, expireSeconds, run);
            return new Tuple<>(true, callback.execute());
        } finally {
            run.set(false);
            unlock(redisTemplate, key, valueId);
        }
    }

    /**
     * 在锁中执行，获取不到锁时定时尝试
     *
     * @param redisTemplate   RedisTemplate
     * @param key             锁定的key
     * @param expireSeconds   锁定时间
     * @param tryLockInterval 尝试获取锁的时间间隔，默认100ms
     * @param callback        锁定期间回调
     * @return 回调执行结果
     */
    public static <T> T doInLock(RedisTemplate redisTemplate, String key,
                                 long expireSeconds, Integer tryLockInterval, RedisLockCallback<T> callback) {
        final String valueId = System.currentTimeMillis() + "-"
                + UUID.randomUUID().toString().replaceAll("-", "")
                + "-" + expireSeconds * 1000;
        int interval = tryLockInterval == null ? 100 : tryLockInterval;
        while (true) {
            if (!lock(redisTemplate, key, valueId, expireSeconds)) {
                try {
                    Thread.sleep(interval);
                    continue;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                return callback.execute();
            } finally {
                unlock(redisTemplate, key, valueId);
            }
        }
    }

    /**
     * 在锁中执行（自动续期），获取不到锁时定时尝试
     *
     * @param redisTemplate   RedisTemplate
     * @param key             锁定的key
     * @param expireSeconds   锁定时间
     * @param tryLockInterval 尝试获取锁的时间间隔，默认100ms
     * @param callback        锁定期间回调
     * @return 回调执行结果
     */
    public static <T> T doInLockAutoRenewal(RedisTemplate redisTemplate, String key,
                                            long expireSeconds, Integer tryLockInterval,
                                            RedisLockCallback<T> callback) {
        final String valueId = System.currentTimeMillis() + "-"
                + UUID.randomUUID().toString().replaceAll("-", "")
                + "-" + expireSeconds * 1000;
        int interval = tryLockInterval == null ? 100 : tryLockInterval;
        while (true) {
            if (!lock(redisTemplate, key, valueId, expireSeconds)) {
                try {
                    Thread.sleep(interval);
                    continue;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            final AtomicBoolean run = new AtomicBoolean(true);
            try {
                autoRenewal(redisTemplate, key, valueId, expireSeconds, run);
                return callback.execute();
            } finally {
                run.set(false);
                unlock(redisTemplate, key, valueId);
            }
        }
    }

    private static void autoRenewal(final RedisTemplate redisTemplate, final String key, final String value,
                                    final long expireSeconds, final AtomicBoolean run) {
        final long delay = (expireSeconds * 1000) * 2 / 3;
        if (delay > 0) {
            TimerTask renewalTask = new SelfRefTimerTask((final TimerTask rt) -> {
                try {
                    if (run != null && run.get()) {
                        if (log.isDebugEnabled()) {
                            log.debug("RENEWAL for key={}", key);
                        }
                        redisTemplate.expire(key, expireSeconds, TimeUnit.SECONDS);
                    } else {
                        TimerTask cancelTask = new SelfRefTimerTask((TimerTask ct) -> {
                            try {
                                if (log.isDebugEnabled()) {
                                    log.debug("cancel RENEWAL task for key={},value={}", key, value);
                                }
                                rt.cancel(); // 取消续期任务
                                CANCEL_TASK_MAP.put(TASK_CNT.getAndIncrement(), ct);
                            } catch (Throwable e) {
                                log.error("cancel RENEWAL run fail for key={},value={},msg={}",
                                        key, value, e.getMessage());
                            }
                        });
                        LOCK_RENEWAL_TIMER.schedule(cancelTask, 0);
                    }
                } catch (Throwable e) {
                    log.error("RENEWAL run fail for key={},value={},msg={}", key, value, e.getMessage());
                }
            });
            LOCK_RENEWAL_TIMER.scheduleAtFixedRate(renewalTask, delay, delay); // 定时续期
        }
    }

    /**
     * 增量到设定的阈值
     * <p>如果${delta} < ${th}, 增量失败, 直接返回目标差值</p>
     * <p>如果${delta} + val <= ${th}, 增量成功, 返回增量后的值</p>
     * <p>如果${delta} + val > ${th}, 增量失败, 返回目标差值(${th} - val - ${delta})</p>
     *
     * @param redisTemplate redisTemplate
     * @param key           键
     * @param delta         增量
     * @param th            阈值
     */
    public static long incrbyUntilTh(RedisTemplate redisTemplate, String key, long delta, long th) {
        if (delta > th) {
            return th - delta;
        }
        DefaultRedisScript<Long> script = RedisScriptGenerator.generator("incrby-until-th.lua", Long.class);
        final Object ret = redisTemplate.execute(script, Collections.singletonList(key),
                new Object[] {String.valueOf(delta), String.valueOf(th)});
        return Long.valueOf(String.valueOf(ret));
    }

    /**
     * 减量到设定的阈值
     * <p>如果减完后大于等于${th}, 直接返回减完后剩余值</p>
     * <p>如果减完后小于阈值, 设定剩余值为阈值，返回剩余值</p>
     *
     * @param redisTemplate redisTemplate
     * @param key           键
     * @param delta         减量
     * @param th            阈值
     */
    public static long decrbyUntilTh(RedisTemplate redisTemplate, String key, long delta, long th) {
        DefaultRedisScript<Long> script = RedisScriptGenerator.generator("decrby-until-th.lua", Long.class);
        final Object ret = redisTemplate.execute(script, Collections.singletonList(key),
                new Object[] {String.valueOf(delta), String.valueOf(th)});
        return Long.valueOf(String.valueOf(ret));
    }

}
