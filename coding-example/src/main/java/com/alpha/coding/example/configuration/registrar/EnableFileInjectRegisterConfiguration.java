package com.alpha.coding.example.configuration.registrar;

import org.springframework.context.annotation.Configuration;

import com.alpha.coding.common.bean.fileinject.annotation.EnableFileInject;
import com.alpha.coding.common.bean.fileinject.annotation.EnableFileInjects;

/**
 * EnableFileInjectRegisterConfiguration
 *
 * @version 1.0
 * Date: 2020-03-21
 */
@EnableFileInjects({
        @EnableFileInject(basePath = "${file1.path}/a",
                includeFilters = {@EnableFileInject.Filter(pattern = {"*.yml"})}),
        @EnableFileInject(basePath = "${file2.path}/b",
                excludeFilters = {@EnableFileInject.Filter(pattern = {"*.yml"})})
})
@Configuration
public class EnableFileInjectRegisterConfiguration {
}
