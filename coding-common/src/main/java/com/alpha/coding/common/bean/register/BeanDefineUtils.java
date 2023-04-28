package com.alpha.coding.common.bean.register;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.expression.StandardBeanExpressionResolver;
import org.springframework.core.env.Environment;
import org.springframework.util.StringValueResolver;

import com.alpha.coding.common.bean.spi.RegisterBeanDefinitionContext;

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

    public static <T> BeanDefinitionBuilder setIfAbsent(BeanDefinitionBuilder builder, Environment environment,
                                                        String property, List<String> keys, Class<T> clz,
                                                        T defaultVal, Function<Object, Object> postMapper,
                                                        Map<String, Object> propertyMap) {
        final Object val = fetchProperty(environment, keys, clz, defaultVal);
        if (val != null) {
            builder.addPropertyValue(property, postMapper == null ? val : postMapper.apply(val));
        }
        propertyMap.put(property, val);
        return builder;
    }

    public static <T> Object fetchProperty(Environment environment, List<String> keys, Class<T> clz, T defaultVal) {
        return fetchProperty(environment, keys, null, clz, defaultVal);
    }

    public static <T> Object fetchProperty(Environment environment, List<String> keys, Predicate<String> valueFilter,
                                           Class<T> clz, T defaultVal) {
        if (keys == null || keys.size() == 0) {
            return defaultVal;
        }
        return keys.stream()
                .filter(environment::containsProperty)
                .filter(k -> (valueFilter == null || valueFilter.test(environment.getProperty(k))))
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

    public static <T> T resolveValue(RegisterBeanDefinitionContext context, String valueExpr, Class<T> type) {
        if (valueExpr == null) {
            return null;
        }
        final BeanFactory beanFactory = context.getBeanFactory();
        final DefaultListableBeanFactory defaultListableBeanFactory =
                beanFactory instanceof DefaultListableBeanFactory ? (DefaultListableBeanFactory) beanFactory :
                        new DefaultListableBeanFactory(beanFactory);
        final Environment environment = context.getEnvironment();
        final StringValueResolver stringValueResolver = environment::resolvePlaceholders;
        final String value = stringValueResolver.resolveStringValue(valueExpr);
        BeanExpressionResolver beanExpressionResolver = defaultListableBeanFactory.getBeanExpressionResolver();
        if (beanExpressionResolver == null) {
            final ClassLoader classLoader = context.getResourceLoader().getClassLoader();
            beanExpressionResolver = new StandardBeanExpressionResolver(classLoader);
            defaultListableBeanFactory.setBeanClassLoader(classLoader);
        }
        final Object evaluate = beanExpressionResolver.evaluate(value,
                new BeanExpressionContext(defaultListableBeanFactory, null));
        TypeConverter converter = defaultListableBeanFactory.getTypeConverter();
        return converter.convertIfNecessary(evaluate, type);
    }

    public static <T> T resolveValue(ApplicationContext context, String valueExpr, Class<T> type) {
        if (valueExpr == null) {
            return null;
        }
        final Environment environment = context.getEnvironment();
        final StringValueResolver stringValueResolver = environment::resolvePlaceholders;
        final String value = stringValueResolver.resolveStringValue(valueExpr);
        if (context instanceof ConfigurableApplicationContext) {
            final ConfigurableListableBeanFactory beanFactory =
                    ((ConfigurableApplicationContext) context).getBeanFactory();
            BeanExpressionResolver beanExpressionResolver = beanFactory.getBeanExpressionResolver();
            final Object evaluate = beanExpressionResolver.evaluate(value,
                    new BeanExpressionContext(beanFactory, null));
            TypeConverter converter = beanFactory.getTypeConverter();
            return converter.convertIfNecessary(evaluate, type);
        } else {
            final AutowireCapableBeanFactory autowireCapableBeanFactory = context.getAutowireCapableBeanFactory();
            if (autowireCapableBeanFactory instanceof DefaultListableBeanFactory) {
                DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) autowireCapableBeanFactory;
                BeanExpressionResolver beanExpressionResolver = beanFactory.getBeanExpressionResolver();
                final Object evaluate = beanExpressionResolver.evaluate(value,
                        new BeanExpressionContext(beanFactory, null));
                TypeConverter converter = beanFactory.getTypeConverter();
                return converter.convertIfNecessary(evaluate, type);
            } else {
                throw new IllegalArgumentException("ApplicationContext Not Supported");
            }
        }
    }

}
