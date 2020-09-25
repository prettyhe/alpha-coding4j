package com.alpha.coding.common.bean.register;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * EnvironmentPropertySourcesChangeBeanFactoryProcessor
 *
 * @version 1.0
 * Date: 2020/9/14
 */
@Slf4j
public class EnvironmentPropertySourcesChangeBeanFactoryProcessor implements BeanFactoryPostProcessor, PriorityOrdered {

    @Setter
    private Environment environment;
    @Setter
    private volatile Set<String> propertySourceNames;
    @Setter
    private BiConsumer<ConfigurableListableBeanFactory, Environment> onChanged;
    @Setter
    private Integer initIndex; // 初始量
    @Setter
    private AtomicInteger globalIndex; // 全局偏移

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        boolean changed = false;
        if (environment instanceof ConfigurableEnvironment) {
            ConfigurableEnvironment env = (ConfigurableEnvironment) environment;
            Set<String> names = new LinkedHashSet<>();
            env.getPropertySources().forEach(s -> names.add(s.getName()));
            if (propertySourceNames == null || propertySourceNames.size() != names.size()) {
                changed = true;
            } else {
                final Iterator<String> it1 = propertySourceNames.iterator();
                final Iterator<String> it2 = names.iterator();
                while (it1.hasNext()) {
                    if (!Objects.equals(it1.next(), it2.next())) {
                        changed = true;
                        break;
                    }
                }
            }
            if (changed) {
                log.info("Environment PropertySources has changed, before:{}, after:{}, this.order:{}",
                        propertySourceNames, names, getOrder());
                if (onChanged != null) {
                    onChanged.accept(beanFactory, environment);
                }
            }
        }
    }

    @Override
    public int getOrder() {
        if (globalIndex == null) {
            return initIndex == null ? Ordered.LOWEST_PRECEDENCE : initIndex;
        }
        return Ordered.LOWEST_PRECEDENCE - globalIndex.get() + (initIndex == null ? 0 : initIndex);
    }
}
