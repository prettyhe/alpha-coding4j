package com.alpha.coding.common.redis;

import java.util.Arrays;
import java.util.Optional;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.SerializationCodec;
import org.redisson.config.Config;
import org.redisson.config.ReadMode;
import org.redisson.connection.balancer.RoundRobinLoadBalancer;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.alpha.coding.common.utils.StringUtils;

import lombok.Setter;

/**
 * RedissonConfiguration
 *
 * @version 1.0
 * Date: 2020-03-20
 */
@Configuration
public class RedissonConfiguration implements EnvironmentAware {

    @Setter
    private Environment environment;

    @Bean("defaultRedissonClient")
    public RedissonClient redissonClient() {
        if (StringUtils.isNotBlank(environment.getProperty("redis.sentinel.nodes"))) {
            return sentinelClient();
        }
        if (StringUtils.isNotBlank(environment.getProperty("redis.cluster.nodes"))) {
            return clusterClient();
        }
        return singleServerClient();
    }

    /**
     * 单机配置
     * <li>redis.host</li>
     * <li>redis.port</li>
     * <li>redis.password</li>
     * <li>redis.db</li>
     */
    private RedissonClient singleServerClient() {
        Config config = new Config();
        config.setCodec(new SerializationCodec());
        config.useSingleServer()
                .setAddress("redis://" + environment.getProperty("redis.host")
                        + ":" + environment.getProperty("redis.port", "6379"))
                .setPassword(environment.getProperty("redis.password"))
                .setIdleConnectionTimeout(10000)
                .setPingConnectionInterval(2000)
                .setConnectTimeout(10000)
                .setTimeout(3000)
                .setRetryInterval(1500)
                .setRetryAttempts(3)
                .setSubscriptionsPerConnection(5)
                .setSubscriptionConnectionMinimumIdleSize(5)
                .setSubscriptionConnectionPoolSize(50)
                .setConnectionMinimumIdleSize(10)
                .setConnectionPoolSize(64)
                .setDatabase(environment.getProperty("redis.db", Integer.class, 0));
        return Redisson.create(config);
    }

    /**
     * 哨兵配置
     * <li>redis.sentinel.master:主服务器的名称</li>
     * <li>redis.sentinel.nodes:192.168.1.100:6379,192.168.1.101:6379</li>
     * <li>redis.sentinel.password</li>
     * <li>redis.db</li>
     * <li>redis.sentinel.readMode:SLAVE/MASTER/MASTER_SLAVE</li>
     */
    private RedissonClient sentinelClient() {
        Config config = new Config();
        config.setCodec(new SerializationCodec());
        config.useSentinelServers()
                .addSentinelAddress(Arrays.stream(environment.getProperty("redis.sentinel.nodes").split(","))
                        .map(String::trim).map(p -> "redis://" + p).toArray(String[]::new))
                .setMasterName(environment.getProperty("redis.sentinel.master"))
                .setReadMode(Arrays.stream(ReadMode.values())
                        .filter(p -> p.name()
                                .equalsIgnoreCase(environment.getProperty("redis.sentinel.readMode", "MASTER_SLAVE")))
                        .findAny().orElse(ReadMode.MASTER_SLAVE))
                .setPassword(Optional.ofNullable(environment.getProperty("redis.sentinel.password"))
                        .orElse(environment.getProperty("redis.password")))
                .setIdleConnectionTimeout(10000)
                .setPingConnectionInterval(2000)
                .setConnectTimeout(10000)
                .setTimeout(3000)
                .setRetryInterval(1500)
                .setRetryAttempts(3)
                .setLoadBalancer(new RoundRobinLoadBalancer())
                .setSubscriptionsPerConnection(5)
                .setSubscriptionConnectionMinimumIdleSize(5)
                .setSubscriptionConnectionPoolSize(50)
                .setMasterConnectionMinimumIdleSize(10)
                .setMasterConnectionPoolSize(64)
                .setSlaveConnectionMinimumIdleSize(10)
                .setSlaveConnectionPoolSize(64)
                .setDatabase(environment.getProperty("redis.db", Integer.class, 0));
        return Redisson.create(config);
    }

    /**
     * 集群配置
     * <li>redis.cluster.nodes:192.168.1.100:6379,192.168.1.101:6379</li>
     * <li>redis.cluster.password</li>
     * <li>redis.sentinel.readMode:SLAVE/MASTER/MASTER_SLAVE</li>
     * <li>redis.cluster.scanInterval:扫描间隔</li>
     */
    private RedissonClient clusterClient() {
        Config config = new Config();
        config.setCodec(new SerializationCodec());
        config.useClusterServers()
                .addNodeAddress(Arrays.stream(environment.getProperty("redis.cluster.nodes").split(","))
                        .map(String::trim).map(p -> "redis://" + p).toArray(String[]::new))
                .setReadMode(Arrays.stream(ReadMode.values())
                        .filter(p -> p.name()
                                .equalsIgnoreCase(environment.getProperty("redis.cluster.readMode", "MASTER_SLAVE")))
                        .findAny().orElse(ReadMode.MASTER_SLAVE))
                .setPassword(Optional.ofNullable(environment.getProperty("redis.cluster.password"))
                        .orElse(environment.getProperty("redis.password")))
                .setIdleConnectionTimeout(10000)
                .setPingConnectionInterval(2000)
                .setConnectTimeout(10000)
                .setTimeout(3000)
                .setRetryInterval(1500)
                .setRetryAttempts(3)
                .setLoadBalancer(new RoundRobinLoadBalancer())
                .setSubscriptionsPerConnection(5)
                .setSubscriptionConnectionMinimumIdleSize(5)
                .setSubscriptionConnectionPoolSize(50)
                .setMasterConnectionMinimumIdleSize(10)
                .setMasterConnectionPoolSize(64)
                .setSlaveConnectionMinimumIdleSize(10)
                .setSlaveConnectionPoolSize(64)
                .setScanInterval(environment.getProperty("redis.cluster.scanInterval", Integer.class, 2000));
        return Redisson.create(config);
    }

}
