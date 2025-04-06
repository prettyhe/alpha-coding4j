package com.alpha.coding.common.redis.message;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Supplier;

import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * RedisMessage
 *
 * @version 1.0
 * Date: 2020-02-17
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface RedisMessage {

    /**
     * topic，适用于发布与订阅
     */
    String[] topic() default {};

    /**
     * topic正则表达式，适用于订阅
     */
    String[] topicPattern() default {};

    /**
     * key序列化工具提供者
     */
    Class<? extends Supplier<RedisSerializer>> redisKeySerializerSupplier() default StringRedisSerializerProvider.class;

    /**
     * value序列化工具提供者
     */
    Class<? extends Supplier<RedisSerializer>> redisValueSerializerSupplier() default NoneRedisSerializerProvider.class;

    /**
     * 是否自动注入到{RedisMessageListenerContainer}
     */
    boolean injectAllListenerContainer() default true;

    /**
     * injectAllListenerContainer为false时指定{RedisMessageListenerContainer}
     */
    String[] listenerContainer() default {};

}
