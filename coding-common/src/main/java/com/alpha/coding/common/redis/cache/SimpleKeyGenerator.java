package com.alpha.coding.common.redis.cache;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import com.alpha.coding.common.utils.MD5Utils;

/**
 * SimpleKeyGenerator
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class SimpleKeyGenerator implements KeyGenerator {

    private ConcurrentHashMap<String, String> keyMap = new ConcurrentHashMap(64);

    @Override
    public Object generate(Object target, Method method, String methodSignature, Object... params) {
        if (methodSignature == null) {
            return "_$CACHE_NULL_KEY$_";
        }
        String val = keyMap.get(methodSignature);
        if (val == null) {
            val = "_$CACHE_KEY$_" + MD5Utils.md5(methodSignature);
            keyMap.put(methodSignature, val);
        }
        return val;
    }
}
