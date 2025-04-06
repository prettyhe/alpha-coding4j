package com.alpha.coding.common.redis.cache.serializer;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.text.StrSubstitutor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alpha.coding.common.assist.compiler.JDKCompilerHelper;
import com.alpha.coding.common.utils.ClassUtils;
import com.alpha.coding.common.utils.IOUtils;
import com.google.common.reflect.TypeToken;

import lombok.extern.slf4j.Slf4j;

/**
 * AutoJsonRedisSerializer
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class AutoJsonRedisSerializer implements RedisSerializer {

    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final Map<String, TypeReference> TYPE_CACHE = new ConcurrentHashMap<>(64);
    private static final AtomicLong COUNT = new AtomicLong(1);
    private static final Class<?> BYTE_ARRAY_TYPE = new byte[0].getClass();

    private final Class<?> targetType;
    private final Type genericType;
    private final Type type;

    public AutoJsonRedisSerializer(Class<?> targetType, Type genericType) {
        this.targetType = targetType;
        this.genericType = genericType;
        this.type = genericType == null ? null : TypeToken.of(genericType).getType();
    }

    /**
     * 通过动态编译类生成TypeReference
     */
    private TypeReference loadTypeReference(Type genericType) {
        if (genericType == null || targetType.isPrimitive() || genericType.equals(targetType)) {
            return null;
        }
        final String key = genericType.toString();
        if (TYPE_CACHE.get(key) == null) {
            synchronized(TYPE_CACHE) {
                if (TYPE_CACHE.get(key) == null) {
                    try {
                        Resource[] resources = new PathMatchingResourcePatternResolver()
                                .getResources("classpath:/resource/TypeReference.tpl");
                        if (resources != null && resources.length > 0) {
                            try (InputStream inputStream = resources[0].getInputStream()) {
                                final String tpl = IOUtils.readFromInputStream(inputStream, CHARSET, true);
                                Map params = new HashMap();
                                params.put("package", this.getClass().getPackage().getName() + ".JSONTypeReference");
                                params.put("classCnt", COUNT.getAndIncrement());
                                params.put("genericType", key);
                                StrSubstitutor sub = new StrSubstitutor(params);
                                String srcCode = sub.replace(tpl);
                                final String javaName = params.get("package") + ".TypeReference"
                                        + params.get("classCnt") + ".java";
                                if (log.isDebugEnabled()) {
                                    log.debug("generate class for file: {} with srcCode: \n{}", javaName, srcCode);
                                }
                                String className = javaName.substring(0, javaName.length() - ".java".length());
                                Class<?> clz = null;
                                final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                                try {
                                    clz = JDKCompilerHelper.getJdkCompiler().compile(className, srcCode,
                                            contextClassLoader, null, -1);
                                } catch (Exception e) {
                                    clz = JDKCompilerHelper.getJdkCompiler(contextClassLoader)
                                            .compile(className, srcCode,
                                                    contextClassLoader, null, -1);
                                }
                                if (log.isDebugEnabled()) {
                                    log.debug("generate class for {} get TypeReference: {}",
                                            key, clz == null ? null : clz.getName());
                                }
                                if (clz == null) {
                                    throw new ClassNotFoundException(className);
                                }
                                TYPE_CACHE.put(key, (TypeReference) (ClassUtils.newInstance(clz)));
                                if (log.isDebugEnabled()) {
                                    log.debug("generate class for {} get Type: {}",
                                            key, TYPE_CACHE.get(key).getType());
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("generate TypeReference for {} fail", key, e);
                    }
                }
            }
        }
        return TYPE_CACHE.get(key);
    }

    @Override
    public byte[] serialize(Object o) throws SerializationException {
        if (o == null) {
            return new byte[0];
        } else if (o instanceof byte[]) {
            return (byte[]) o;
        } else if (o instanceof String) {
            return ((String) o).getBytes(CHARSET);
        }
        return JSON.toJSONString(o).getBytes(CHARSET);
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        if (targetType.equals(BYTE_ARRAY_TYPE)) {
            return bytes;
        }
        if (bytes == null || bytes.length == 0 || Void.TYPE.equals(targetType)) {
            return null;
        }
        if (String.class.equals(targetType)) {
            return new String(bytes, CHARSET);
        }
        if (genericType == null || type == null) {
            try {
                return JSON.parseObject(new String(bytes, CHARSET), targetType);
            } catch (Exception e) {
                return JSON.parse(new String(bytes, CHARSET));
            }
        } else {
            return JSON.parseObject(new String(bytes, CHARSET), type);
        }
    }

}
