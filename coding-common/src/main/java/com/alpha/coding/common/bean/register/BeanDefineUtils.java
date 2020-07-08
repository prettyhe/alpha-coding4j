package com.alpha.coding.common.bean.register;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.core.env.Environment;

/**
 * BeanDefineUtils
 *
 * @version 1.0
 * Date: 2020/6/12
 */
public class BeanDefineUtils {

    public static <T> BeanDefinitionBuilder setIfAbsent(BeanDefinitionBuilder builder, Environment environment,
                                                        String property, List<String> keys, Class<T> clz,
                                                        T defaultVal, Map<String, Object> propertyMap) {
        final Object val = fetchProperty(environment, keys, clz, defaultVal);
        if (val != null) {
            builder.addPropertyValue(property, val);
        }
        propertyMap.put(property, val);
        return builder;
    }

    public static <T> Object fetchProperty(Environment environment, List<String> keys, Class<T> clz, T defaultVal) {
        if (keys == null || keys.size() == 0) {
            return defaultVal;
        }
        return keys.stream()
                .filter(k -> environment.containsProperty(k))
                .findFirst()
                .map(k -> {
                    if (clz != null && defaultVal != null) {
                        return environment.getProperty(k, clz, defaultVal);
                    } else if (clz != null) {
                        return environment.getProperty(k, clz);
                    } else {
                        return environment.getProperty(k);
                    }
                }).orElse(defaultVal);
    }

}
