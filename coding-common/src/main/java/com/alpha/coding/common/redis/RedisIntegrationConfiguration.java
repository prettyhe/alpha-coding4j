package com.alpha.coding.common.redis;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.alpha.coding.common.bean.register.BeanDefineUtils;
import com.alpha.coding.common.utils.StringUtils;

import lombok.Setter;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolAbstract;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;

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
        Optional.ofNullable((Integer) BeanDefineUtils.fetchProperty(environment,
                Arrays.asList("redis.pool.max_active", "redis.pool.maxActive"), StringUtils::isNotBlank,
                Integer.class, null)).ifPresent(config::setMaxTotal);
        Optional.ofNullable((Integer) BeanDefineUtils.fetchProperty(environment,
                Arrays.asList("redis.pool.max_idle", "redis.pool.maxIdle"), StringUtils::isNotBlank,
                Integer.class, null)).ifPresent(config::setMaxIdle);
        Optional.ofNullable((Long) BeanDefineUtils.fetchProperty(environment,
                Arrays.asList("redis.pool.max_wait", "redis.pool.maxWait"), StringUtils::isNotBlank,
                Long.class, null)).ifPresent(config::setMaxWaitMillis);
        Optional.ofNullable((Boolean) BeanDefineUtils.fetchProperty(environment,
                Arrays.asList("redis.pool.test_on_borrow", "redis.pool.testOnBorrow"), StringUtils::isNotBlank,
                Boolean.class, null)).ifPresent(config::setTestOnBorrow);
        Optional.ofNullable((Boolean) BeanDefineUtils.fetchProperty(environment,
                Arrays.asList("redis.pool.test_on_return", "redis.pool.testOnReturn"), StringUtils::isNotBlank,
                Boolean.class, null)).ifPresent(config::setTestOnReturn);
        return config;
    }

    @Lazy
    @Bean("defaultRedisConfiguration")
    public RedisConfiguration redisStandaloneConfiguration() {
        // RedisSentinel
        if (StringUtils.isNotBlank(environment.getProperty("redis.sentinel.nodes"))) {
            RedisSentinelConfiguration redisSentinelConfiguration = new RedisSentinelConfiguration(
                    environment.getProperty("redis.sentinel.master"),
                    org.springframework.util.StringUtils
                            .commaDelimitedListToSet(environment.getProperty("redis.sentinel.nodes")));
            redisSentinelConfiguration.setSentinelPassword(
                    RedisPassword.of(environment.getProperty("redis.sentinel.password")));
            redisSentinelConfiguration.setPassword(RedisPassword.of(environment.getProperty("redis.password")));
            redisSentinelConfiguration.setDatabase(environment.getProperty("redis.db", Integer.class, 0));
            return redisSentinelConfiguration;
        }
        // RedisCluster
        if (StringUtils.isNotBlank(environment.getProperty("redis.cluster.nodes"))) {
            RedisClusterConfiguration redisClusterConfiguration =
                    new RedisClusterConfiguration(org.springframework.util.StringUtils
                            .commaDelimitedListToSet(environment.getProperty("redis.cluster.nodes")));
            Optional.ofNullable((Integer) BeanDefineUtils.fetchProperty(environment,
                    Arrays.asList("redis.cluster.max-redirects", "redis.cluster.maxRedirects",
                            "redis.cluster.max_redirects"), StringUtils::isNotBlank, Integer.class, null))
                    .ifPresent(redisClusterConfiguration::setMaxRedirects);
            redisClusterConfiguration.setPassword(
                    RedisPassword.of(Optional.ofNullable(environment.getProperty("redis.cluster.password"))
                            .orElse(environment.getProperty("redis.password"))));
            return redisClusterConfiguration;
        }
        // RedisStandalone
        final RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(environment.getProperty("redis.host"));
        configuration.setPort(environment.getProperty("redis.port", Integer.class, 6379));
        configuration.setDatabase(environment.getProperty("redis.db", Integer.class, 0));
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
        final RedisConfiguration redisConfiguration = redisStandaloneConfiguration();
        if (redisConfiguration instanceof RedisSentinelConfiguration) {
            return new JedisConnectionFactory((RedisSentinelConfiguration) redisConfiguration,
                    jedisClientConfiguration);
        } else if (redisConfiguration instanceof RedisClusterConfiguration) {
            return new JedisConnectionFactory((RedisClusterConfiguration) redisConfiguration,
                    jedisClientConfiguration);
        } else {
            return new JedisConnectionFactory((RedisStandaloneConfiguration) redisConfiguration,
                    jedisClientConfiguration);
        }
    }

    /**
     * 默认的RedisTemplate，key、hashKey使用StringRedisSerializer
     */
    @Lazy
    @Bean
    public RedisTemplate<Object, Object> redisTemplate() {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        // keySerializer使用独立对象
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        return template;
    }

    /**
     * 原生的RedisTemplate，使用默认的RedisSerializer
     */
    @Lazy
    @Bean("rawRedisTemplate")
    public RedisTemplate<Object, Object> rawRedisTemplate() {
        final RedisTemplate<Object, Object> template = new RedisTemplate<>();
        // keySerializer使用独立对象
        template.setKeySerializer(new JdkSerializationRedisSerializer(template.getClass().getClassLoader()));
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
    }

    /**
     * 全String类型的RedisTemplate，所有RedisSerializer都使用StringRedisSerializer
     */
    @Lazy
    @Bean("stringRedisTemplate")
    public RedisTemplate stringRedisTemplate() {
        final StringRedisTemplate stringRedisTemplate = new StringRedisTemplate(jedisConnectionFactory());
        // keySerializer使用独立对象
        stringRedisTemplate.setKeySerializer(new StringRedisSerializer());
        return stringRedisTemplate;
    }

    /**
     * jdkValueRedisTemplate，key、hashKey使用StringRedisSerializer，value、hashValue使用JdkSerializationRedisSerializer
     */
    @Lazy
    @Bean("jdkValueRedisTemplate")
    public RedisTemplate<String, Object> jdkValueRedisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        final JdkSerializationRedisSerializer jdkSerializationRedisSerializer = new JdkSerializationRedisSerializer();
        template.setDefaultSerializer(jdkSerializationRedisSerializer);
        // keySerializer使用独立对象
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jdkSerializationRedisSerializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jdkSerializationRedisSerializer);
        return template;
    }

    /**
     * jdkHashValueRedisTemplate，key、value、hashKey使用StringRedisSerializer，hashValue使用JdkSerializationRedisSerializer
     */
    @Lazy
    @Bean("jdkHashValueRedisTemplate")
    public RedisTemplate<String, Object> jdkHashValueRedisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        final JdkSerializationRedisSerializer jdkSerializationRedisSerializer = new JdkSerializationRedisSerializer();
        template.setDefaultSerializer(jdkSerializationRedisSerializer);
        // keySerializer使用独立对象
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jdkSerializationRedisSerializer);
        return template;
    }

    @Lazy
    @Bean("defaultJedisPool")
    public JedisPoolAbstract jedisPool() {
        if (StringUtils.isNotBlank(environment.getProperty("redis.sentinel.nodes"))) {
            return new JedisSentinelPool(environment.getProperty("redis.sentinel.master"),
                    org.springframework.util.StringUtils
                            .commaDelimitedListToSet(environment.getProperty("redis.sentinel.nodes")),
                    jedisPoolConfig(),
                    environment.getProperty("redis.timeout", Integer.class),
                    environment.getProperty("redis.password"));
        }
        return new JedisPool(jedisPoolConfig(),
                environment.getProperty("redis.host"),
                environment.getProperty("redis.port", Integer.class, 6379),
                environment.getProperty("redis.timeout", Integer.class),
                environment.getProperty("redis.password"));
    }

    @Lazy
    @Bean("defaultRedisBean")
    public RedisBean redisBean() {
        final RedisBean redisBean = new RedisBean();
        redisBean.setPool(jedisPool());
        redisBean.setDb(environment.getProperty("redis.db", Integer.class, 0));
        return redisBean;
    }

}
