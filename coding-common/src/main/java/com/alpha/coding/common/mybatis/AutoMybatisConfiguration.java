package com.alpha.coding.common.mybatis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AutoMybatisConfiguration
 *
 * @version 1.0
 * Date: 2020-03-21
 */
@Configuration
public class AutoMybatisConfiguration {

    @Bean("defaultIbatisConfiguration")
    public org.apache.ibatis.session.Configuration configuration() {
        final org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        return configuration;
    }

}
