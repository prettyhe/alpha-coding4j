package com.alpha.coding.common.redis.cache;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeansException;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;

import com.alpha.coding.bo.base.Tuple;
import com.alpha.coding.bo.function.ThrowableSupplier;
import com.alpha.coding.common.aop.assist.AopHelper;
import com.alpha.coding.common.aop.assist.ExpressionKey;
import com.alpha.coding.common.aop.assist.JoinOperationContext;
import com.alpha.coding.common.aop.assist.JoinOperationMetadata;
import com.alpha.coding.common.aop.assist.SpelExpressionParserFactory;
import com.alpha.coding.common.redis.cache.annotation.CacheIgnore;
import com.alpha.coding.common.redis.cache.annotation.RedisCacheEvict;
import com.alpha.coding.common.redis.cache.annotation.RedisCachePut;
import com.alpha.coding.common.redis.cache.annotation.RedisCacheable;
import com.alpha.coding.common.redis.cache.serializer.AutoJsonRedisSerializer;
import com.alpha.coding.common.utils.CompressUtils;
import com.alpha.coding.common.utils.InvokeUtils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * RedisCacheAspect
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Data
@Slf4j
public class RedisCacheAspect implements ApplicationContextAware {

    @NotNull
    private ApplicationContext applicationContext;

    private CacheManager localCacheManager;

    private CacheConfig cacheConfig;

    private String nullValueSubstitute = "\u0001\u0001\u0001"; // null值缓存时的转义值
    private KeyGenerator keyGenerator = new SimpleKeyGenerator();
    private ExpressionParser expressionParser = new SpelExpressionParserFactory().getInstance();
    private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
    private static final Object NO_RESULT = new Object();
    private final Charset DEFAULT_CS = Charset.forName("UTF-8");
    private final Map<ExpressionKey, Expression> keyCache = new ConcurrentHashMap<>(64);
    private final Map<AnnotatedElementKey, JoinOperationMetadata> metadataCache = new ConcurrentHashMap<>(1024);
    private final Map<String, InvokeUtils.InvokeLock> loadLockCache = new ConcurrentHashMap<>(256);
    private final Map<AnnotatedElementKey, RedisSerializer> serializerCache = new ConcurrentHashMap<>(256);

    /**
     * 切面逻辑
     */
    public Object doCacheAspect(final ProceedingJoinPoint joinPoint) throws Throwable {
        final Tuple<CacheConfig, CacheOperation> cacheConfigCacheOperationTuple = parseCacheConfigOperation(joinPoint);
        if (cacheConfigCacheOperationTuple == null || cacheConfigCacheOperationTuple.getF() == null
                || cacheConfigCacheOperationTuple.getS() == null) {
            return joinPoint.proceed();
        }
        final CacheConfig cacheConfig = cacheConfigCacheOperationTuple.getF();
        final CacheOperation cacheOperation = cacheConfigCacheOperationTuple.getS();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        final Cache localCache = loadLocalCache(cacheConfig, signature);
        final Class<?> targetClass = AopHelper.getTargetClass(joinPoint.getTarget());
        AnnotatedElementKey metadataCacheKey = new AnnotatedElementKey(signature.getMethod(), targetClass);
        if (metadataCache.get(metadataCacheKey) == null) {
            metadataCache.put(metadataCacheKey, new JoinOperationMetadata(signature.getMethod(),
                    targetClass, signature));
        }
        JoinOperationMetadata metadata = metadataCache.get(metadataCacheKey);
        JoinOperationContext cacheOperationContext = new JoinOperationContext(metadata,
                joinPoint.getArgs(), joinPoint.getTarget());
        final String cacheKey = buildCacheKey(cacheOperationContext, cacheConfig);
        if (log.isDebugEnabled()) {
            log.debug("generate cache key: {}", cacheKey);
        }
        final ThrowableSupplier<Object> proceedLocalCacheFunction = () -> {
            final Object result = joinPoint.proceed();
            putIntoLocalCache(localCache, cacheConfig, cacheKey, result);
            return result;
        };
        // cache del
        if (cacheOperation == CacheOperation.DEL) {
            final RedisTemplate redisTemplate = loadRedisTemplate(cacheConfig);
            if (redisTemplate != null) {
                final RedisSerializer redisSerializer = getRedisSerializer(cacheConfig, metadataCacheKey, metadata);
                final byte[] rawKey = redisSerializer.serialize(cacheKey);
                redisTemplate.execute((RedisConnection connection) -> connection.del(rawKey));
            }
            if (localCache != null) {
                localCache.evict(cacheKey);
            }
            return joinPoint.proceed();
        }
        // cache put
        if (cacheOperation == CacheOperation.PUT) {
            final RedisTemplate redisTemplate = loadRedisTemplate(cacheConfig);
            if (redisTemplate == null) {
                log.error("CacheError: InvalidRedisTemplate");
                return proceedLocalCacheFunction.get();
            }
            return conditionSyncLoad(joinPoint, cacheConfig, redisTemplate,
                    getRedisSerializer(cacheConfig, metadataCacheKey, metadata),
                    metadata, cacheKey, localCache);
        }
        // fetch from local cache
        if (localCache != null) {
            final Cache.ValueWrapper valueWrapper = localCache.get(cacheKey);
            if (valueWrapper != null) {
                if (cacheConfig.isCacheNull() && nullValueSubstitute.equals(valueWrapper.get())) {
                    return null;
                }
                return valueWrapper.get();
            }
        }
        // fetch from redis
        final RedisTemplate redisTemplate = loadRedisTemplate(cacheConfig);
        if (redisTemplate == null) {
            log.error("CacheError: InvalidRedisTemplate");
            return proceedLocalCacheFunction.get();
        }
        final RedisSerializer redisSerializer = getRedisSerializer(cacheConfig, metadataCacheKey, metadata);
        final Object redisVal = redisTemplate.execute((RedisConnection connection) ->
                connection.get(redisSerializer.serialize(cacheKey)));
        if (redisVal == null) {
            return conditionSyncLoad(joinPoint, cacheConfig, redisTemplate, redisSerializer,
                    metadata, cacheKey, localCache);
        } else {
            try {
                final Object result = parseOriginValue(cacheConfig, (byte[]) redisVal, redisSerializer);
                putIntoLocalCache(localCache, cacheConfig, cacheKey, result);
                return result;
            } catch (IOException e) {
                log.warn("parseFromCache fail for key={},msg={}", cacheKey, e.getMessage());
                return conditionSyncLoad(joinPoint, cacheConfig, redisTemplate, redisSerializer,
                        metadata, cacheKey, localCache);
            }
        }
    }

    private CacheManager loadCacheManager(CacheConfig cacheConfig) {
        if (StringUtils.isNotBlank(cacheConfig.getLocalCacheManager())) {
            return applicationContext.getBean(cacheConfig.getLocalCacheManager(), CacheManager.class);
        }
        return localCacheManager;
    }

    private Cache loadLocalCache(CacheConfig cacheConfig, MethodSignature signature) {
        if (StringUtils.isNotBlank(cacheConfig.getLocalName())) {
            CacheManager cacheManager = loadCacheManager(cacheConfig);
            if (cacheManager == null || cacheManager.getCache(cacheConfig.getLocalName()) == null) {
                throw new IllegalArgumentException("Cannot find cache named '" +
                        cacheConfig.getLocalName() + "' for " + signature.toLongString());
            }
            return cacheManager.getCache(cacheConfig.getLocalName());
        }
        return null;
    }

    private RedisTemplate loadRedisTemplate(CacheConfig cacheConfig) {
        if (StringUtils.isNotBlank(cacheConfig.getRedisTemplateName())) {
            return applicationContext.getBean(cacheConfig.getRedisTemplateName(), RedisTemplate.class);
        }
        return cacheConfig.getRedisTemplate();
    }

    private Object conditionSyncLoad(final ProceedingJoinPoint joinPoint,
                                     final CacheConfig cacheConfig,
                                     final RedisTemplate redisTemplate,
                                     final RedisSerializer redisSerializer,
                                     final JoinOperationMetadata metadata,
                                     final String cacheKey,
                                     final Cache localCache) throws Throwable {
        if (!cacheConfig.isSyncLoad()) {
            Object result = joinPoint.proceed();
            putIntoCache(joinPoint.getArgs(), result, cacheKey, cacheConfig,
                    redisTemplate, redisSerializer, localCache);
            return result;
        } else {
            return InvokeUtils.syncInvoke(loadLockCache, cacheKey, null,
                    () -> joinPoint.proceed(),
                    val -> putIntoCache(joinPoint.getArgs(), val, cacheKey, cacheConfig,
                            redisTemplate, redisSerializer, localCache)
            ).getData();
        }
    }

    private RedisSerializer getRedisSerializer(CacheConfig cacheConfig,
                                               AnnotatedElementKey metadataCacheKey,
                                               JoinOperationMetadata metadata) {
        RedisSerializer redisSerializer = serializerCache.get(metadataCacheKey);
        if (redisSerializer == null) {
            redisSerializer = new AutoJsonRedisSerializer(metadata.getMethod().getReturnType(),
                    metadata.getMethod().getGenericReturnType());
            serializerCache.put(metadataCacheKey, redisSerializer);
        }
        return redisSerializer;
    }

    private Object parseOriginValue(CacheConfig cacheConfig, byte[] rawValue,
                                    RedisSerializer redisSerializer) throws IOException {
        byte[] realRawValue = cacheConfig.isGzip() ? CompressUtils.unGzip(rawValue) : rawValue;
        if (cacheConfig.isCacheNull()
                && Arrays.equals(redisSerializer.serialize(nullValueSubstitute), realRawValue)) {
            return null;
        }
        return redisSerializer.deserialize(realRawValue);
    }

    private void putIntoCache(Object[] args, Object returnValue,
                              final String cacheKey,
                              final CacheConfig cacheConfig,
                              final RedisTemplate redisTemplate,
                              final RedisSerializer redisSerializer,
                              final Cache localCache) {
        try {
            Object value = returnValue;
            if (returnValue == null) {
                if (!cacheConfig.isCacheNull()) {
                    return;
                } else {
                    value = nullValueSubstitute;
                }
            }
            final long expireSeconds = cacheConfig.getExpireStrategy() != null ? cacheConfig.getExpireStrategy()
                    .calculateExpire(args, returnValue) : cacheConfig.getExpire();
            if (expireSeconds == 0) {
                return;
            }
            try {
                final byte[] rawKey = redisSerializer.serialize(cacheKey);
                final byte[] rawValue = cacheConfig.isGzip() ? CompressUtils.gzip(redisSerializer.serialize(value))
                        : redisSerializer.serialize(value);
                redisTemplate.execute((RedisConnection connection) -> {
                    try {
                        final Method setEx =
                                connection.getClass().getMethod("setEx", byte[].class, long.class, byte[].class);
                        setEx.invoke(connection, rawKey, expireSeconds, rawValue);
                    } catch (Exception e) {
                        log.warn("doInRedis:setEx invoke fail,{}", e.getMessage());
                    }
                    return null;
                });
            } catch (IOException e) {
                log.error("putIntoCache fail for {}, {}", cacheKey, e);
            }
        } finally {
            putIntoLocalCache(localCache, cacheConfig, cacheKey, returnValue);
        }
    }

    private void putIntoLocalCache(Cache cache, CacheConfig config, String cacheKey, Object value) {
        if (cache == null) {
            return;
        }
        if (value == null) {
            if (config.isCacheNull()) {
                cache.put(cacheKey, nullValueSubstitute);
            }
        } else {
            cache.put(cacheKey, value);
        }
    }

    private Tuple<CacheConfig, CacheOperation> parseCacheConfigOperation(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        final Class<?> targetClass = AopHelper.getTargetClass(joinPoint.getTarget());
        final Method method = AopHelper.getTargetMethod(targetClass, signature.getMethod());
        if (method.isAnnotationPresent(CacheIgnore.class)) {
            return null;
        }
        if (targetClass.isAnnotationPresent(CacheIgnore.class)) {
            return null;
        }
        final CacheConfig cacheConfig = new CacheConfig();
        if (this.cacheConfig != null) { // 默认使用全局配置
            cacheConfig.setRedisTemplateName(this.cacheConfig.getRedisTemplateName())
                    .setRedisTemplate(this.cacheConfig.getRedisTemplate())
                    .setExpire(this.cacheConfig.getExpire())
                    .setName(this.cacheConfig.getName())
                    .setKey(this.cacheConfig.getKey())
                    .setGzip(this.cacheConfig.isGzip())
                    .setCacheNull(this.cacheConfig.isCacheNull())
                    .setSyncLoad(this.cacheConfig.isSyncLoad())
                    .setExpireStrategy(this.cacheConfig.getExpireStrategy())
                    .setLocalCacheManager(this.cacheConfig.getLocalCacheManager());
        }
        CacheOperation operation = CacheOperation.GET;
        if (targetClass.isAnnotationPresent(RedisCacheable.class)) {
            updateCacheConfig(cacheConfig, targetClass.getAnnotation(RedisCacheable.class), null);
        }
        if (method.isAnnotationPresent(RedisCacheable.class)) {
            updateCacheConfig(cacheConfig, method.getAnnotation(RedisCacheable.class), null);
        }
        if (targetClass.isAnnotationPresent(RedisCachePut.class)) {
            updateCacheConfig(cacheConfig, null, targetClass.getAnnotation(RedisCachePut.class));
            operation = CacheOperation.PUT;
        }
        if (method.isAnnotationPresent(RedisCachePut.class)) {
            updateCacheConfig(cacheConfig, null, method.getAnnotation(RedisCachePut.class));
            operation = CacheOperation.PUT;
        }
        if (method.isAnnotationPresent(RedisCacheEvict.class)) {
            updateCacheConfig(cacheConfig, method.getAnnotation(RedisCacheEvict.class));
            operation = CacheOperation.DEL;
        }
        return new Tuple<>(cacheConfig, operation);
    }

    private void updateCacheConfig(CacheConfig cacheConfig, RedisCacheable cacheable, RedisCachePut cachePut) {
        final String redisBeanName = cacheable == null ? cachePut.redisBean() : cacheable.redisBean();
        if (StringUtils.isNotBlank(redisBeanName)) {
            cacheConfig.setRedisTemplateName(redisBeanName);
        }
        final String name = cacheable == null ? cachePut.name() : cacheable.name();
        if (StringUtils.isNotBlank(name)) {
            cacheConfig.setName(name);
        }
        if (cacheable != null && !cacheable.expireStrategy().equals(ExpireStrategy.class)) {
            try {
                cacheConfig.setExpireStrategy(applicationContext.getBean(cacheable.expireStrategy()));
            } catch (BeansException e) {
                log.warn("init ExpireStrategy fail for {}, msg={}", cacheable.key(), e.getMessage());
            }
        } else if (cachePut != null && !cachePut.expireStrategy().equals(ExpireStrategy.class)) {
            try {
                cacheConfig.setExpireStrategy(applicationContext.getBean(cachePut.expireStrategy()));
            } catch (BeansException e) {
                log.warn("init ExpireStrategy fail for {}, msg={}", cachePut.key(), e.getMessage());
            }
        }
        final String localName = cacheable == null ? cachePut.localName() : cacheable.localName();
        if (StringUtils.isNotBlank(localName)) {
            cacheConfig.setLocalName(localName);
        }
        final String localCacheManager =
                cacheable == null ? cachePut.localCacheManager() : cacheable.localCacheManager();
        if (StringUtils.isNotBlank(localCacheManager)) {
            cacheConfig.setLocalCacheManager(localCacheManager);
        }
        cacheConfig.setExpire(cacheable == null ? cachePut.expire() : cacheable.expire())
                .setGzip(cacheable == null ? cachePut.gzip() : cacheable.gzip())
                .setKey(cacheable == null ? cachePut.key() : cacheable.key())
                .setCacheNull(cacheable == null ? cachePut.cacheNull() : cacheable.cacheNull())
                .setSyncLoad(cacheable == null ? cachePut.syncLoad() : cacheable.syncLoad());
    }

    private void updateCacheConfig(CacheConfig cacheConfig, RedisCacheEvict cacheEvict) {
        if (StringUtils.isNotBlank(cacheEvict.redisBean())) {
            cacheConfig.setRedisTemplateName(cacheEvict.redisBean());
        }
        if (StringUtils.isNotBlank(cacheEvict.name())) {
            cacheConfig.setName(cacheEvict.name());
        }
        if (StringUtils.isNotBlank(cacheEvict.localName())) {
            cacheConfig.setLocalName(cacheEvict.localName());
        }
        if (StringUtils.isNotBlank(cacheEvict.localCacheManager())) {
            cacheConfig.setLocalCacheManager(cacheEvict.localCacheManager());
        }
        cacheConfig.setKey(cacheEvict.key());
    }

    private String buildCacheKey(JoinOperationContext context, CacheConfig cacheConfig) {
        String keySuffix = "";
        if (StringUtils.isNotBlank(cacheConfig.getKey())) {
            final Object key = generateKey(cacheConfig.getKey(), context.getMethodCacheKey(),
                    AopHelper.createEvaluationContext(context.getMetadata().getMethod(), context.getArgs(),
                            context.getTarget(), context.getMetadata().getTargetClass(), NO_RESULT,
                            applicationContext, getParameterNameDiscoverer()));
            keySuffix = String.valueOf(key);
        } else {
            keySuffix = String.valueOf(keyGenerator.generate(context.getTarget(), context.getMetadata().getMethod(),
                    context.getMethodSignature(), context.getArgs()));
        }
        if (StringUtils.isNotBlank(cacheConfig.getName())) {
            return cacheConfig.getName() + ":" + keySuffix;
        } else {
            return "ReCh:" + keySuffix;
        }
    }

    private Object generateKey(String keyExpression, AnnotatedElementKey methodKey, EvaluationContext evalContext) {
        return AopHelper.getExpression(this.keyCache, methodKey, keyExpression, getExpressionParser())
                .getValue(evalContext);
    }

}
