package com.alpha.coding.common.redis.cache;

import java.lang.reflect.Method;

/**
 * KeyGenerator
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public interface KeyGenerator {

    Object generate(Object target, Method method, String methodSignature, Object... params);

}
