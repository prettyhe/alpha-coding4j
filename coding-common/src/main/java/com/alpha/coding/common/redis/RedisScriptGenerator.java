package com.alpha.coding.common.redis;

import java.util.HashMap;
import java.util.Map;

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

    private static final Map<String, DefaultRedisScript> CACHE = new HashMap<>();

    /**
     * 生成供RedisTemplate执行的脚本
     *
     * @param luaFileName lua脚本名
     * @param returnType  脚本返回类型，必须设置
     * @return 返回结果
     */
    public static <T> DefaultRedisScript<T> generator(String luaFileName, Class<T> returnType) {
        String key = luaFileName + "#" + returnType.getName();
        if (CACHE.containsKey(key)) {
            return CACHE.get(key);
        }
        DefaultRedisScript<T> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/redis-lua/" + luaFileName)));
        script.setResultType(returnType); // Must Set
        CACHE.put(key, script);
        return script;
    }

}
