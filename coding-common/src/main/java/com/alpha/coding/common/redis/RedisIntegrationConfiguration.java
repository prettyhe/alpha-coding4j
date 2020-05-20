package com.alpha.coding.common.redis;

import java.time.Duration;

import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import lombok.Setter;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * RedisIntegrationConfiguration
 *
 * @version 1.0
 * Date: 2020-03-20
 */
@Configuration
public class RedisIntegrationConfiguration implements EnvironmentAware {

    @Setter
    private Environment environment;

    @Lazy
    @Bean("defaultJedisPoolConfig")
    public JedisPoolConfig jedisPoolConfig() {
        final JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(environment.getProperty("redis.pool.max_active", Integer.class));
        config.setMaxIdle(environment.getProperty("redis.pool.max_idle", Integer.class));
        config.setMaxWaitMillis(environment.getProperty("redis.pool.max_wait", Integer.class));
        config.setTestOnBorrow(environment.getProperty("redis.pool.test_on_borrow", Boolean.class));
        config.setTestOnReturn(environment.getProperty("redis.pool.test_on_return", Boolean.class));
        return config;
    }

    @Lazy
    @Bean("defaultRedisStandaloneConfiguration")
    public RedisStandaloneConfiguration redisStandaloneConfiguration() {
        final RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(environment.getProperty("redis.host"));
        configuration.setPort(environment.getProperty("redis.port", Integer.class));
        configuration.setDatabase(environment.getProperty("redis.db", Integer.class));
        configuration.setPassword(RedisPassword.of(environment.getProperty("redis.password")));
        return configuration;
    }

    @Lazy
    @Bean("defaultJedisConnectionFactory")
    public JedisConnectionFactory jedisConnectionFactory() {
        JedisClientConfiguration jedisClientConfiguration = JedisClientConfiguration
                .builder()
                .usePooling()
                .poolConfig(jedisPoolConfig())
                .and()
                .connectTimeout(Duration.ofMillis(environment.getProperty("redis.timeout", Long.class)))
                .readTimeout(Duration.ofMillis(environment.getProperty("redis.timeout", Long.class)))
                .build();
        return new JedisConnectionFactory(redisStandaloneConfiguration(), jedisClientConfiguration);
    }

    @Lazy
    @Bean("stringRedisTemplate")
    public RedisTemplate stringRedisTemplate() {
        return new StringRedisTemplate(jedisConnectionFactory());
    }

    @Lazy
    @Bean("defaultJedisPool")
    public JedisPool jedisPool() {
        return new JedisPool(jedisPoolConfig(),
                environment.getProperty("redis.host"),
                environment.getProperty("redis.port", Integer.class),
                environment.getProperty("redis.timeout", Integer.class),
                environment.getProperty("redis.password"));
    }

    @Lazy
    @Bean("defaultRedisBean")
    public RedisBean redisBean() {
        final RedisBean redisBean = new RedisBean();
        redisBean.setPool(jedisPool());
        redisBean.setDb(environment.getProperty("redis.db", Integer.class));
        return redisBean;
    }

}
