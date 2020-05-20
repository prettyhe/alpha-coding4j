package com.alpha.coding.example.configuration.registrar;

import org.springframework.context.annotation.Configuration;

import com.alpha.coding.common.event.EnableAutoConfigEventBus;
import com.alpha.coding.example.event.CacheEventType;

/**
 * EnableAutoConfigEventBusConfiguration
 *
 * @version 1.0
 * Date: 2020-03-21
 */
@Configuration
@EnableAutoConfigEventBus(eventIdentity = CacheEventType.class)
public class EnableAutoConfigEventBusConfiguration {
}
