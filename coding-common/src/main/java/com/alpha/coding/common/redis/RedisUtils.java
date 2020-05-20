package com.alpha.coding.common.redis;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.alpha.coding.bo.base.Tuple;
import com.alpha.coding.common.utils.IOUtils;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

/**
 * RedisUtils
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class RedisUtils {

    private static final Map<String, String> LUA_MAP = new HashMap<>();
    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    private static final String LOCK_SUCCESS = "OK";
    private static final Long RELEASE_SUCCESS = 1L;

    static {
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver()
                    .getResources("classpath:/redis-lua/*.lua");
            for (Resource resource : resources) {
                String lua = IOUtils.readFromInputStream(resource.getInputStream(), DEFAULT_CHARSET, true);
                String path = resource.getFilename();
                int start = path.lastIndexOf("/") > 0 ? path.lastIndexOf("/") : 0;
                int end = path.lastIndexOf(".") > 0 ? path.lastIndexOf(".") : 0;
                String key = path.substring(start, end);
                LUA_MAP.put(key, lua);
            }
        } catch (IOException e) {
            log.error("load lua script error", e);
        }
    }

    /**
     * 根据脚本名获取脚本内容
     *
     * @param scriptName 脚本名
     */
    public static String getScript(String scriptName) {
        return LUA_MAP.get(scriptName);
    }

    /**
     * 根据脚本名执行脚本
     *
     * @param jedis      redis客户端
     * @param scriptName 脚本名
     * @param initParams 脚本初始化参数
     * @param keys       key列表，NotNull
     * @param args       参数列表，NotNull
     *
     * @return 脚本执行结果
     */
    public static Object eval(Jedis jedis, String scriptName,
                              Object[] initParams,
                              @NotNull Iterable<String> keys, @NotNull Iterable<String> args) {
        String script = LUA_MAP.get(scriptName);
        if (script == null) {
            throw new NullPointerException("null script for " + scriptName);
        }
        if (keys == null) {
            throw new NullPointerException("null keys");
        }
        if (args == null) {
            throw new NullPointerException("null args");
        }
        if (initParams != null && initParams.length > 0) {
            script = String.format(script, initParams);
        }
        return jedis.eval(script, Lists.newArrayList(keys), Lists.newArrayList(args));
    }

    /**
     * 根据脚本内容执行脚本
     *
     * @param jedis  redis客户端
     * @param script 脚本内容
     * @param keys   key列表，NotNull
     * @param args   参数列表，NotNull
     *
     * @return 脚本执行结果
     */
    public static Object eval(Jedis jedis, String script,
                              @NotNull Iterable<String> keys, @NotNull Iterable<String> args) {
        if (keys == null) {
            throw new NullPointerException("null keys");
        }
        if (args == null) {
            throw new NullPointerException("null args");
        }
        return jedis.eval(script, Lists.newArrayList(keys), Lists.newArrayList(args));
    }

    /**
     * 获取redis锁，返回元组第一个字段表示是否获取成功，第二个表示锁的到期时间
     *
     * @param jedis         jedis
     * @param key           key
     * @param expireSeconds 锁定秒数
     *
     * @return 返回结果元组
     */
    @Deprecated
    public static Tuple<Boolean, Long> lock(Jedis jedis, String key, long expireSeconds) {
        long expireAt = System.currentTimeMillis() + expireSeconds * 1000;
        List<String> keys = Lists.newArrayList(key);
        List<String> args = Lists.newArrayList(String.valueOf(expireAt), String.valueOf(expireSeconds));
        List<String> ret = (List<String>) eval(jedis, getScript("redis-lock"), keys, args);
        return new Tuple<>(String.valueOf(ret.get(0)).equals("1"), Long.valueOf(ret.get(1)));
    }

    /**
     * 释放redis锁
     *
     * @param jedis jedis
     * @param key   key
     */
    @Deprecated
    public static void unlock(Jedis jedis, String key) {
        jedis.del(key);
    }

    /**
     * 尝试获取锁
     *
     * @param jedis         Redis客户端
     * @param key           锁key
     * @param value         锁值，期望为客户端唯一标识符
     * @param expireSeconds 锁定时长
     *
     * @return 是否获取成功
     */
    public static boolean tryLock(Jedis jedis, String key, String value, long expireSeconds) {
        String result = jedis.set(key, value, new SetParams().nx().ex((int) expireSeconds));
        return LOCK_SUCCESS.equals(result);
    }

    /**
     * 尝试获取锁
     *
     * @param jedis        Redis客户端
     * @param key          锁key
     * @param value        锁值，期望为客户端唯一标识符
     * @param expireMillis 锁定时长(ms)
     *
     * @return 是否获取成功
     */
    public static boolean tryLockMillis(Jedis jedis, String key, String value, long expireMillis) {
        String result = jedis.set(key, value, new SetParams().nx().px(expireMillis));
        return LOCK_SUCCESS.equals(result);
    }

    /**
     * 尝试释放锁
     *
     * @param jedis Redis客户端
     * @param key   锁key
     * @param value 锁值，期望为客户端唯一标识符
     *
     * @return 是否释放成功
     */
    public static boolean tryUnlock(Jedis jedis, String key, String value) {
        Object result = eval(jedis, getScript("del-by-val"),
                Lists.<String>newArrayList(key), Lists.<String>newArrayList(value));
        return RELEASE_SUCCESS.equals(result);
    }

    /**
     * 尝试释放锁，不使用lua，非原子性的，可能导致释放失败
     *
     * @param jedis Redis客户端
     * @param key   锁key
     * @param value 锁值，期望为客户端唯一标识符
     *
     * @return 是否释放成功
     */
    public static boolean tryUnlockWithoutLua(Jedis jedis, String key, String value) {
        final String val = jedis.get(key);
        if (value.equals(val)) {
            jedis.del(key);
            return true;
        }
        return false;
    }

    /**
     * redis锁后业务回调
     */
    public static interface RedisLockCallback<T> {
        T execute();
    }

    /**
     * 在锁中执行，获取不到锁时立即返回
     *
     * @param jedis         jedis
     * @param key           锁定的key
     * @param expireSeconds 锁定时间
     * @param useLua        是否使用lua，根据server支持与否传入
     * @param callback      锁定期间回调
     *
     * @return 执行结果元组(是否成功获取锁, 回调执行结果)
     */
    public static <T> Tuple<Boolean, T> doInLockReturnOnLockFail(Jedis jedis, String key,
                                                                 long expireSeconds, Boolean useLua,
                                                                 RedisLockCallback<T> callback) {
        return doInLockMillisReturnOnLockFail(jedis, key, expireSeconds * 1000, useLua, callback);
    }

    /**
     * 在锁中执行，获取不到锁时立即返回
     *
     * @param jedis        jedis
     * @param key          锁定的key
     * @param expireMillis 锁定时间(ms)
     * @param useLua       是否使用lua，根据server支持与否传入
     * @param callback     锁定期间回调
     *
     * @return 执行结果元组(是否成功获取锁, 回调执行结果)
     */
    public static <T> Tuple<Boolean, T> doInLockMillisReturnOnLockFail(Jedis jedis, String key,
                                                                       long expireMillis, Boolean useLua,
                                                                       RedisLockCallback<T> callback) {
        String valueId = UUID.randomUUID().toString().replaceAll("-", "")
                + "-" + (System.currentTimeMillis() + expireMillis * 1000);
        if (!tryLockMillis(jedis, key, valueId, expireMillis)) {
            return new Tuple<>(false, null);
        }
        try {
            return new Tuple<>(true, callback.execute());
        } finally {
            if (useLua != null && useLua.booleanValue()) {
                tryUnlock(jedis, key, valueId);
            } else {
                tryUnlockWithoutLua(jedis, key, valueId); // 使用非原子性操作，可能释放失败
            }
        }
    }

    /**
     * 在锁中执行，获取不到锁时定时尝试
     *
     * @param jedis           jedis
     * @param key             锁定的key
     * @param expireSeconds   锁定时间
     * @param tryLockInterval 尝试获取锁的时间间隔，默认100ms
     * @param useLua          是否使用lua，根据server支持与否传入
     * @param callback        锁定期间回调
     *
     * @return 回调执行结果
     */
    public static <T> T doInLock(Jedis jedis, String key, long expireSeconds,
                                 Integer tryLockInterval, Boolean useLua,
                                 RedisLockCallback<T> callback) {
        return doInLockMillis(jedis, key, expireSeconds * 1000, tryLockInterval, useLua, callback);
    }

    /**
     * 在锁中执行，获取不到锁时定时尝试
     *
     * @param jedis           jedis
     * @param key             锁定的key
     * @param expireMillis    锁定时间
     * @param tryLockInterval 尝试获取锁的时间间隔，默认100ms
     * @param useLua          是否使用lua，根据server支持与否传入
     * @param callback        锁定期间回调
     *
     * @return 回调执行结果
     */
    public static <T> T doInLockMillis(Jedis jedis, String key, long expireMillis,
                                       Integer tryLockInterval, Boolean useLua,
                                       RedisLockCallback<T> callback) {
        return doInLockMillisTryMax(jedis, key, expireMillis, tryLockInterval, null, useLua, callback);
    }

    /**
     * 在锁中执行，获取不到锁时定时尝试，最大尝试指定次数
     *
     * @param jedis           jedis
     * @param key             锁定的key
     * @param expireSeconds   锁定时间
     * @param tryLockInterval 尝试获取锁的时间间隔，默认100ms
     * @param tryLockMaxTimes 最大尝试获取锁次数，默认无限次
     * @param useLua          是否使用lua，根据server支持与否传入
     * @param callback        锁定期间回调
     *
     * @return 回调执行结果
     */
    public static <T> T doInLockTryMax(Jedis jedis, String key, long expireSeconds,
                                       Integer tryLockInterval, Integer tryLockMaxTimes, Boolean useLua,
                                       RedisLockCallback<T> callback) {
        return doInLockMillisTryMax(jedis, key, expireSeconds * 1000,
                tryLockInterval, tryLockMaxTimes, useLua, callback);
    }

    /**
     * 在锁中执行，获取不到锁时定时尝试，最大尝试指定次数
     *
     * @param jedis           jedis
     * @param key             锁定的key
     * @param expireMillis    锁定时间(ms)
     * @param tryLockInterval 尝试获取锁的时间间隔，默认100ms
     * @param tryLockMaxTimes 最大尝试获取锁次数，默认无限次
     * @param useLua          是否使用lua，根据server支持与否传入
     * @param callback        锁定期间回调
     *
     * @return 回调执行结果
     */
    public static <T> T doInLockMillisTryMax(Jedis jedis, String key, long expireMillis,
                                             Integer tryLockInterval, Integer tryLockMaxTimes, Boolean useLua,
                                             RedisLockCallback<T> callback) {
        String valueId = UUID.randomUUID().toString().replaceAll("-", "")
                + "-" + (System.currentTimeMillis() + expireMillis);
        int interval = tryLockInterval == null ? 100 : tryLockInterval;
        int times = 0;
        int maxTryTimes = tryLockMaxTimes == null ? -1 : tryLockMaxTimes.intValue();
        while (maxTryTimes < 0 || times < maxTryTimes) {
            if (!tryLockMillis(jedis, key, valueId, expireMillis)) {
                try {
                    times++;
                    Thread.sleep(interval);
                    continue;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                return callback.execute();
            } finally {
                if (useLua != null && useLua.booleanValue()) {
                    tryUnlock(jedis, key, valueId);
                } else {
                    tryUnlockWithoutLua(jedis, key, valueId); // 使用非原子性操作，可能释放失败
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("RedisLock try {} times for {} and not success", times, key);
        }
        return null;
    }

}
