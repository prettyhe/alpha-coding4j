package com.alpha.coding.common.redis;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.MDC;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.alpha.coding.bo.base.MapThreadLocalAdaptor;
import com.alpha.coding.bo.base.Tuple;
import com.alpha.coding.bo.executor.NamedExecutorPool;
import com.alpha.coding.bo.executor.schedule.ScheduleDelegator;
import com.alpha.coding.bo.executor.schedule.ScheduledTask;
import com.alpha.coding.bo.function.common.Converter;
import com.alpha.coding.common.executor.MDCRunnableWrapper;

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
    private static final ScheduledExecutorService LOCK_RENEWAL_SCHEDULER =
            NamedExecutorPool.newScheduledThreadPool("RedisLockRenewalScheduler",
                    Math.max(Runtime.getRuntime().availableProcessors() * 2, 8));
    private static final ScheduleDelegator LOCK_RENEWAL_SCHEDULER_DELEGATOR =
            ScheduleDelegator.build(LOCK_RENEWAL_SCHEDULER);
    private static final String CLIENT_ID = UUID.randomUUID().toString().replace("-", "").toUpperCase();
    private static transient Method REDIS_TEMPLATE_SET_EX = null;
    private static final StringRedisSerializer STRING_REDIS_SERIALIZER =
            new StringRedisSerializer(StandardCharsets.UTF_8);
    private static final JdkSerializationRedisSerializer JDK_SERIALIZATION_REDIS_SERIALIZER =
            new JdkSerializationRedisSerializer();
    private static final LongRedisSerializer LONG_REDIS_SERIALIZER = new LongRedisSerializer();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(LOCK_RENEWAL_SCHEDULER::shutdown));
    }

    private static class LongRedisSerializer implements RedisSerializer<Long> {

        @Override
        public byte[] serialize(Long val) throws SerializationException {
            return val == null ? new byte[0] : String.valueOf(val).getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public Long deserialize(byte[] bytes) throws SerializationException {
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            try {
                return (Long) Converter.convertToNumber.apply(new String(bytes, StandardCharsets.UTF_8), Long.class);
            } catch (UnsupportedOperationException e) {
                final Object val = JDK_SERIALIZATION_REDIS_SERIALIZER.deserialize(bytes);
                return (Long) Converter.convertToNumber.apply(val, Long.class);
            }
        }
    }

    /**
     * 管道操作，拿到原始byte[]数据，需要自己实现反序列化
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static List<Object> executePipelined(final RedisTemplate redisTemplate, final RedisCallback<?> action) {
        return (List<Object>) redisTemplate.execute((RedisCallback<List<Object>>) connection -> {
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
        });
    }

    /**
     * 获取当前客户端标识
     */
    private static String getClientIdentity() {
        return CLIENT_ID + "_t_" + Thread.currentThread().getId();
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
    @Deprecated
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static boolean lock(final RedisTemplate redisTemplate, final String key,
                               final String uqVal, final long expireSeconds) {
        DefaultRedisScript<String> script = RedisScriptGenerator.generator("set-nx-ex.lua", String.class);
        final Object ret = redisTemplate.execute(script, STRING_REDIS_SERIALIZER, STRING_REDIS_SERIALIZER,
                Collections.singletonList(key), uqVal, String.valueOf(expireSeconds));
        return ret != null && LOCK_SUCCESS.equals(String.valueOf(ret));
    }

    /**
     * 加锁操作
     *
     * @param redisTemplate redisTemplate
     * @param key           锁定的key
     * @param expireSeconds 锁定时长
     * @return 是否获取锁
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static boolean lock(final RedisTemplate redisTemplate, final String key, final long expireSeconds) {
        final String clientIdentity = getClientIdentity();
        DefaultRedisScript<Long> script = RedisScriptGenerator.generator("lock.lua", Long.class);
        final Object ret = redisTemplate.execute(script, STRING_REDIS_SERIALIZER, LONG_REDIS_SERIALIZER,
                Collections.singletonList(key), clientIdentity, String.valueOf(expireSeconds * 1000));
        // null=获取到锁，其它表示当前锁的剩余时间
        return ret == null;
    }

    /**
     * 解锁操作
     *
     * @param redisTemplate redisTemplate
     * @param key           锁定的key
     * @param uqVal         获取锁客户端的位移标识
     * @return 是否释放锁
     */
    @Deprecated
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static boolean unlock(RedisTemplate redisTemplate, String key, String uqVal) {
        DefaultRedisScript<Long> script = RedisScriptGenerator.generator("del-by-val.lua", Long.class);
        final Object ret = redisTemplate.execute(script, STRING_REDIS_SERIALIZER, LONG_REDIS_SERIALIZER,
                Collections.singletonList(key), uqVal);
        if (log.isDebugEnabled()) {
            log.debug("unlock result:{} for key:{}, val:{}", ret, key, uqVal);
        }
        return ret != null && UNLOCK_SUCCESS.equals(String.valueOf(ret));
    }

    /**
     * 解锁操作
     *
     * @param redisTemplate redisTemplate
     * @param key           锁定的key
     * @return 是否释放锁
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static boolean unlock(RedisTemplate redisTemplate, String key) {
        final String clientIdentity = getClientIdentity();
        DefaultRedisScript<Long> script = RedisScriptGenerator.generator("unlock.lua", Long.class);
        final Object ret = redisTemplate.execute(script, STRING_REDIS_SERIALIZER, LONG_REDIS_SERIALIZER,
                Collections.singletonList(key), clientIdentity);
        if (log.isDebugEnabled()) {
            log.debug("unlock result:{} for key:{}, client:{}", ret, key, clientIdentity);
        }
        // null=锁不存在，0=重入释放，1=彻底释放
        return "1".equals(String.valueOf(ret));
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
    @SuppressWarnings({"rawtypes"})
    public static <T> Tuple<Boolean, T> doInLockReturnOnLockFail(RedisTemplate redisTemplate, String key,
                                                                 long expireSeconds, RedisLockCallback<T> callback) {
        if (!lock(redisTemplate, key, expireSeconds)) {
            return new Tuple<>(false, null);
        }
        try {
            return new Tuple<>(true, callback.execute());
        } finally {
            unlock(redisTemplate, key);
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
    @SuppressWarnings({"rawtypes"})
    public static <T> Tuple<Boolean, T> doInLockAutoRenewalReturnOnLockFail(RedisTemplate redisTemplate,
                                                                            String key, long expireSeconds,
                                                                            RedisLockCallback<T> callback) {
        if (!lock(redisTemplate, key, expireSeconds)) {
            return new Tuple<>(false, null);
        }
        final AtomicBoolean run = new AtomicBoolean(true);
        try {
            autoRenewal(redisTemplate, key, expireSeconds, run);
            return new Tuple<>(true, callback.execute());
        } finally {
            run.set(false);
            unlock(redisTemplate, key);
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
    @SuppressWarnings({"rawtypes"})
    public static <T> T doInLock(RedisTemplate redisTemplate, String key, long expireSeconds,
                                 Integer tryLockInterval, RedisLockCallback<T> callback) {
        int interval = tryLockInterval == null ? 100 : tryLockInterval;
        while (true) {
            if (!lock(redisTemplate, key, expireSeconds)) {
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
                unlock(redisTemplate, key);
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
    @SuppressWarnings({"rawtypes"})
    public static <T> T doInLockAutoRenewal(RedisTemplate redisTemplate, String key, long expireSeconds,
                                            Integer tryLockInterval, RedisLockCallback<T> callback) {
        int interval = tryLockInterval == null ? 100 : tryLockInterval;
        while (true) {
            if (!lock(redisTemplate, key, expireSeconds)) {
                try {
                    Thread.sleep(interval);
                    continue;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            final AtomicBoolean run = new AtomicBoolean(true);
            try {
                autoRenewal(redisTemplate, key, expireSeconds, run);
                return callback.execute();
            } finally {
                run.set(false);
                unlock(redisTemplate, key);
            }
        }
    }

    /**
     * 自动续期，通过定时任务
     *
     * @param redisTemplate RedisTemplate
     * @param key           续期的key
     * @param expireSeconds 有效期(秒)
     * @param run           释放标识
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void autoRenewal(final RedisTemplate redisTemplate, final String key,
                                   final long expireSeconds, final AtomicBoolean run) {
        final long delay = (expireSeconds * 1000) * 2 / 3;
        if (delay > 0) {
            final ScheduledTask renewalTask = new ScheduledTask()
                    .setInitialDelay(delay).setPeriod(delay)
                    .setScheduledMode(ScheduledTask.ScheduledMode.FixedRate)
                    .setTimeUnit(TimeUnit.MILLISECONDS)
                    .setCommand(t -> {
                        try {
                            if (run != null && run.get()) {
                                // 锁未释放，执行续期
                                if (log.isDebugEnabled()) {
                                    log.debug("(seq:{},cnt:{}) RENEWAL for key={},expireSeconds={}",
                                            t.getTaskId(), t.runCount(), key, expireSeconds);
                                }
                                redisTemplate.expire(key, expireSeconds, TimeUnit.SECONDS);
                            } else {
                                // 锁已释放，修改任务模式
                                t.setPeriod(10);
                                t.setScheduledMode(ScheduledTask.ScheduledMode.Once);
                                t.setCommand(ct -> {
                                    if (log.isDebugEnabled()) {
                                        log.debug("(seq:{},cnt:{}) cancel RENEWAL for key={},expireSeconds={}",
                                                t.getTaskId(), t.runCount(), key, expireSeconds);
                                    }
                                });
                            }
                        } catch (Throwable e) {
                            log.error("(seq:{},cnt:{}) RENEWAL fail for key={},expireSeconds={}",
                                    t.getTaskId(), t.runCount(), key, expireSeconds, e);
                        }
                    });
            LOCK_RENEWAL_SCHEDULER_DELEGATOR.schedule(renewalTask, MDCRunnableWrapper.of(null,
                    MDC.getCopyOfContextMap(), MapThreadLocalAdaptor.getCopyOfContextMap()));
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
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static long incrbyUntilTh(RedisTemplate redisTemplate, String key, long delta, long th) {
        if (delta > th) {
            return th - delta;
        }
        DefaultRedisScript<Long> script = RedisScriptGenerator.generator("incrby-until-th.lua", Long.class);
        final Object ret = redisTemplate.execute(script, STRING_REDIS_SERIALIZER, LONG_REDIS_SERIALIZER,
                Collections.singletonList(key), String.valueOf(delta), String.valueOf(th));
        return Long.parseLong(String.valueOf(ret));
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
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static long decrbyUntilTh(RedisTemplate redisTemplate, String key, long delta, long th) {
        DefaultRedisScript<Long> script = RedisScriptGenerator.generator("decrby-until-th.lua", Long.class);
        final Object ret = redisTemplate.execute(script, STRING_REDIS_SERIALIZER, LONG_REDIS_SERIALIZER,
                Collections.singletonList(key), String.valueOf(delta), String.valueOf(th));
        return Long.parseLong(String.valueOf(ret));
    }

    /**
     * 使用RedisTemplate执行setEX
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void setEX(RedisTemplate redisTemplate, String key, final byte[] rawValue, long expireSeconds) {
        final byte[] rawKey = redisTemplate.getKeySerializer().serialize(key);
        redisTemplate.execute((RedisConnection connection) -> {
            if (REDIS_TEMPLATE_SET_EX != null) {
                try {
                    REDIS_TEMPLATE_SET_EX.invoke(connection, rawKey, expireSeconds, rawValue);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    log.warn("doInRedis:setEx invoke fail,{}", e.getMessage());
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    connection.setEx(rawKey, expireSeconds, rawValue);
                } catch (NoSuchMethodError e) {
                    // 兼容方法不匹配的情况
                    try {
                        REDIS_TEMPLATE_SET_EX = connection.getClass().getMethod("setEx",
                                byte[].class, long.class, byte[].class);
                    } catch (NoSuchMethodException e1) {
                        throw new RuntimeException(e1);
                    }
                    try {
                        REDIS_TEMPLATE_SET_EX.invoke(connection, rawKey, expireSeconds, rawValue);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
                        log.warn("doInRedis:setEx invoke fail,{}", e1.getMessage());
                        throw new RuntimeException(e1);
                    }
                }
            }
            return null;
        });
    }

    /**
     * 使用RedisTemplate获取value原始值
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static byte[] getRaw(RedisTemplate redisTemplate, String key) {
        final byte[] rawKey = redisTemplate.getKeySerializer().serialize(key);
        return (byte[]) redisTemplate.execute((RedisConnection connection) -> connection.get(rawKey));
    }

    /**
     * 限流令牌获取
     *
     * @param redisTemplate redisTemplate
     * @param key           令牌key
     * @param expireSeconds 令牌持有时长
     * @param rateLimit     令牌数阈值
     * @return 是否获取到令牌
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static boolean acquireRateLimit(final RedisTemplate redisTemplate, final String key,
                                           final long expireSeconds, int rateLimit) {
        if (rateLimit <= 0) {
            throw new IllegalArgumentException("rateLimit must positive number!");
        }
        final String clientIdentity = getClientIdentity();
        DefaultRedisScript<Long> script = RedisScriptGenerator.generator("rate-limit-acquire.lua", Long.class);
        final Object ret = redisTemplate.execute(script, STRING_REDIS_SERIALIZER, LONG_REDIS_SERIALIZER,
                Collections.singletonList(key), clientIdentity, String.valueOf(expireSeconds * 1000),
                String.valueOf(rateLimit));
        if (log.isDebugEnabled()) {
            log.debug("acquireRateLimit result:{} for key:{}, client:{}", ret, key, clientIdentity);
        }
        // null=获取到令牌，其它表示当前令牌的剩余时间
        return ret == null;
    }

    /**
     * 限流令牌释放
     *
     * @param redisTemplate redisTemplate
     * @param key           令牌key
     * @return 是否释放令牌
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static boolean releaseRateLimit(RedisTemplate redisTemplate, String key) {
        final String clientIdentity = getClientIdentity();
        DefaultRedisScript<Long> script = RedisScriptGenerator.generator("rate-limit-release.lua", Long.class);
        final Object ret = redisTemplate.execute(script, STRING_REDIS_SERIALIZER, LONG_REDIS_SERIALIZER,
                Collections.singletonList(key), clientIdentity);
        if (log.isDebugEnabled()) {
            log.debug("releaseRateLimit result:{} for key:{}, client:{}", ret, key, clientIdentity);
        }
        // null=限流不存在，0=重入释放，1=彻底释放
        return "1".equals(String.valueOf(ret));
    }

    /**
     * 获取到限流令牌后执行，获取不到限流令牌时立即返回
     *
     * @param redisTemplate RedisTemplate
     * @param key           令牌key
     * @param expireSeconds 令牌持有时间
     * @param rateLimit     令牌数阈值
     * @param callback      令牌持有期间回调
     * @return 执行结果元组(是否成功获取到限流令牌, 回调执行结果)
     */
    @SuppressWarnings({"rawtypes"})
    public static <T> Tuple<Boolean, T> doWithRateLimitReturnOnLockFail(RedisTemplate redisTemplate, String key,
                                                                        long expireSeconds, int rateLimit,
                                                                        RedisLockCallback<T> callback) {
        if (!acquireRateLimit(redisTemplate, key, expireSeconds, rateLimit)) {
            return new Tuple<>(false, null);
        }
        try {
            return new Tuple<>(true, callback.execute());
        } finally {
            releaseRateLimit(redisTemplate, key);
        }
    }

    /**
     * 获取到限流令牌执行(自动续期)，获取不到限流令牌时立即返回
     *
     * @param redisTemplate RedisTemplate
     * @param key           令牌key
     * @param expireSeconds 令牌持有时间
     * @param rateLimit     令牌数阈值
     * @param callback      令牌持有期间回调
     * @return 执行结果元组(是否成功获取到限流令牌, 回调执行结果)
     */
    @SuppressWarnings({"rawtypes"})
    public static <T> Tuple<Boolean, T> doWithRateLimitAutoRenewalReturnOnLockFail(RedisTemplate redisTemplate,
                                                                                   String key, long expireSeconds,
                                                                                   int rateLimit,
                                                                                   RedisLockCallback<T> callback) {
        if (!acquireRateLimit(redisTemplate, key, expireSeconds, rateLimit)) {
            return new Tuple<>(false, null);
        }
        final AtomicBoolean run = new AtomicBoolean(true);
        try {
            autoRenewal(redisTemplate, key, expireSeconds, run);
            return new Tuple<>(true, callback.execute());
        } finally {
            run.set(false);
            releaseRateLimit(redisTemplate, key);
        }
    }

    /**
     * 获取到限流令牌执行，获取不到限流令牌时定时尝试
     *
     * @param redisTemplate   RedisTemplate
     * @param key             令牌key
     * @param expireSeconds   令牌持有时间
     * @param rateLimit       令牌数阈值
     * @param tryLockInterval 尝试获取令牌的时间间隔，默认100ms
     * @param callback        令牌持有期间回调
     * @return 回调执行结果
     */
    @SuppressWarnings({"rawtypes"})
    public static <T> T doWithRateLimit(RedisTemplate redisTemplate, String key, long expireSeconds,
                                        int rateLimit, Integer tryLockInterval, RedisLockCallback<T> callback) {
        int interval = tryLockInterval == null ? 100 : tryLockInterval;
        while (true) {
            if (!acquireRateLimit(redisTemplate, key, expireSeconds, rateLimit)) {
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
                releaseRateLimit(redisTemplate, key);
            }
        }
    }

    /**
     * 获取到限流令牌后执行（自动续期），获取不到限流令牌时定时尝试
     *
     * @param redisTemplate   RedisTemplate
     * @param key             令牌key
     * @param expireSeconds   令牌持有时间
     * @param rateLimit       令牌数阈值
     * @param tryLockInterval 尝试获取限流令牌的时间间隔，默认100ms
     * @param callback        获取到限流令牌后回调
     * @return 回调执行结果
     */
    @SuppressWarnings({"rawtypes"})
    public static <T> T doWithRateLimitAutoRenewal(RedisTemplate redisTemplate, String key, long expireSeconds,
                                                   int rateLimit, Integer tryLockInterval,
                                                   RedisLockCallback<T> callback) {
        int interval = tryLockInterval == null ? 100 : tryLockInterval;
        while (true) {
            if (!acquireRateLimit(redisTemplate, key, expireSeconds, rateLimit)) {
                try {
                    Thread.sleep(interval);
                    continue;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            final AtomicBoolean run = new AtomicBoolean(true);
            try {
                autoRenewal(redisTemplate, key, expireSeconds, run);
                return callback.execute();
            } finally {
                run.set(false);
                releaseRateLimit(redisTemplate, key);
            }
        }
    }

}
