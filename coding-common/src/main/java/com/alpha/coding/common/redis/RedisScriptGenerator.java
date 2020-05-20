package com.alpha.coding.common.redis;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

/**
 * RedisScriptGenerator
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class RedisScriptGenerator {

    private static ConcurrentMap<String, DefaultRedisScript> CACHE = new ConcurrentHashMap<>(16);

    /**
     * 生成供RedisTemplate执行的脚本
     *
     * @param luaFileName lua脚本名
     * @param returnType  脚本返回类型，必须设置
     *
     * @return 返回结果
     */
    public static <T> DefaultRedisScript<T> generator(String luaFileName, Class<T> returnType) {
        String key = luaFileName + "#" + returnType.getName();
        return CACHE.computeIfAbsent(key, k -> {
            DefaultRedisScript<T> script = new DefaultRedisScript<T>();
            script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/redis-lua/" + luaFileName)));
            script.setResultType(returnType); // Must Set
            return script;
        });
    }

}
