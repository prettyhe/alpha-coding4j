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
     * topic
     */
    String[] topic() default {};

    /**
     * topic正则表达式
     */
    String[] topicPattern() default {};

    /**
     * 序列化工具提供者
     */
    Class<? extends Supplier<RedisSerializer>> redisSerializerSupplier() default NoneRedisSerializerProvider.class;

    /**
     * 是否自动注入到所有{RedisMessageListenerContainer}
     */
    boolean injectAllListenerContainer() default true;

    /**
     * injectAllListenerContainer为false时指定{RedisMessageListenerContainer}
     */
    String[] listenerContainer() default {};

}
