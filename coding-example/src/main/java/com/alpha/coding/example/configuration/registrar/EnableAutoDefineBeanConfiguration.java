package com.alpha.coding.example.configuration.registrar;

import org.springframework.context.annotation.Configuration;

import com.alpha.coding.common.bean.define.EnableAutoDefineBean;

/**
 * EnableAutoDefineBeanConfiguration
 *
 * @version 1.0
 * Date: 2020-03-21
 */
@Configuration
@EnableAutoDefineBean(basePackages = {"com.alpha.coding.example"})
public class EnableAutoDefineBeanConfiguration {
}
