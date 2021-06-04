package com.alpha.coding.common.bean.property;

import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.EncodedResource;

import lombok.extern.slf4j.Slf4j;

/**
 * AutoPropertySourceFactory
 *
 * @version 1.0
 * Date: 2020-01-02
 */
@Slf4j
public class AutoPropertySourceFactory extends DefaultPropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        String sourceName = (name == null) ? resource.getResource().getFilename() : name;
        if (sourceName == null) {
            return super.createPropertySource(null, resource);
        }
        if (sourceName.endsWith(".yml") || sourceName.endsWith(".yaml")) {
            try {
                YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
                factory.setResources(resource.getResource());
                factory.afterPropertiesSet();
                Properties properties = factory.getObject();
                return new PropertiesPropertySource(sourceName, properties);
            } catch (Exception e) {
                log.error("load YAML source {} fail", sourceName);
                throw e;
            }
        }
        return super.createPropertySource(name, resource);
    }

}
