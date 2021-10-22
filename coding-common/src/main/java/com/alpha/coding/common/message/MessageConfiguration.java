package com.alpha.coding.common.message;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.alpha.coding.common.bean.init.EnableAutoWarmUp;

/**
 * MessageConfiguration
 *
 * @version 1.0
 * Date: 2021/9/8
 */
@Configuration
@EnableAutoWarmUp
@ComponentScan(basePackageClasses = {MessageConfiguration.class})
public class MessageConfiguration {

}
