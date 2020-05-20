package com.alpha.coding.example.configuration.registrar;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.alpha.coding.common.bean.property.AutoPropertySourceFactory;
import com.alpha.coding.common.redis.EnableRedisIntegration;

/**
 * EnableRedisIntegrationConfiguration
 *
 * @version 1.0
 * Date: 2020-03-22
 */
@Configuration
@EnableRedisIntegration(redisCacheKeyPrefix = "RDS:CH", enableRedisMessageListener = true)
@PropertySource(value = {"classpath:redis.yml"}, factory = AutoPropertySourceFactory.class)
public class EnableRedisIntegrationConfiguration {
}
