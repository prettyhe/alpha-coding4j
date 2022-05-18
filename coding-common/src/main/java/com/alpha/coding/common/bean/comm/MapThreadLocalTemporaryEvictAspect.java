package com.alpha.coding.common.bean.comm;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import com.alpha.coding.bo.base.MapThreadLocalAdaptor;
import com.alpha.coding.common.aop.assist.AopHelper;

import lombok.Data;

/**
 * MapThreadLocalTemporaryEvictAspect
 *
 * @version 1.0
 * Date: 2022/5/8
 */
@Data
public class MapThreadLocalTemporaryEvictAspect {

    /**
     * 配置的需要临时移除的key
     */
    private String[] evictKeys;

    /**
     * 切面方法
     *
     * @param joinPoint 切入点
     * @return 方法执行返回结果
     */
    public Object doAspect(ProceedingJoinPoint joinPoint) throws Throwable {
        final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        final Method method = signature.getMethod();
        final List<String> keys = new ArrayList<>();
        if (evictKeys != null) {
            Collections.addAll(keys, evictKeys);
        }
        Class<?> targetClass = method.getDeclaringClass();
        try {
            final Object target = AopHelper.getTarget(joinPoint.getTarget());
            if (!Proxy.isProxyClass(target.getClass())) {
                targetClass = target.getClass();
            }
        } catch (Exception e) {
            // nothing
        }
        if (targetClass.isAnnotationPresent(MapThreadLocalTemporaryEvict.class)) {
            final MapThreadLocalTemporaryEvict annotation =
                    targetClass.getDeclaredAnnotation(MapThreadLocalTemporaryEvict.class);
            Collections.addAll(keys, annotation.value());
        } else if (method.isAnnotationPresent(MapThreadLocalTemporaryEvict.class)) {
            final MapThreadLocalTemporaryEvict annotation =
                    method.getDeclaredAnnotation(MapThreadLocalTemporaryEvict.class);
            Collections.addAll(keys, annotation.value());
        }
        if (keys.size() == 0) {
            return joinPoint.proceed();
        }
        final Map<String, Object> existMap = new HashMap<>(keys.size() * 2);
        for (String key : keys) {
            if (MapThreadLocalAdaptor.containsKey(key)) {
                existMap.put(key, MapThreadLocalAdaptor.get(key));
            }
        }
        try {
            keys.forEach(MapThreadLocalAdaptor::remove); // 临时删除
            return joinPoint.proceed();
        } finally {
            existMap.forEach(MapThreadLocalAdaptor::put); // 恢复
        }
    }

}
