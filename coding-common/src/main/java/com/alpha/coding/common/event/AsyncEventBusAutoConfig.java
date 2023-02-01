package com.alpha.coding.common.event;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.DependsOn;

import com.alpha.coding.common.event.configuration.EventConfiguration;
import com.alpha.coding.common.event.eventbus.EventBusTemplate;
import com.alpha.coding.common.event.listener.EventListenerTemplate;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * AsyncEventBusAutoConfig
 *
 * @version 1.0
 * Date: 2020-02-19
 */
@Data
@Slf4j
@DependsOn("eventMetaMonitor")
public class AsyncEventBusAutoConfig implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) beanFactory;
        defaultListableBeanFactory.getBeansOfType(EventConfiguration.class).forEach((k, v) -> {
            final String name = v.getIdentity().getName();
            // EventListenerTemplate
            String listenerTemplateName = name + "_listenerTemplate";
            BeanDefinitionBuilder listenerTemplateBeanDefinitionBuilder =
                    BeanDefinitionBuilder.genericBeanDefinition(EventListenerTemplate.class);
            listenerTemplateBeanDefinitionBuilder.addPropertyValue("identity", v.getIdentity());
            defaultListableBeanFactory.registerBeanDefinition(listenerTemplateName,
                    listenerTemplateBeanDefinitionBuilder.getRawBeanDefinition());
            // EventBusTemplate
            String eventBusTemplateName = name + "_eventBusTemplate";
            BeanDefinitionBuilder eventBusTemplateBeanDefinitionBuilder =
                    BeanDefinitionBuilder.genericBeanDefinition(EventBusTemplate.class);
            eventBusTemplateBeanDefinitionBuilder.addPropertyValue("identity", v.getIdentity());
            eventBusTemplateBeanDefinitionBuilder.addPropertyValue("pollingEventInterval",
                    v.getPollingEventInterval());
            eventBusTemplateBeanDefinitionBuilder.addPropertyValue("enableAsyncPost",
                    v.isEnableAsyncPost());
            eventBusTemplateBeanDefinitionBuilder.addPropertyValue("enableEventPostMonitor",
                    v.isEnableEventPostMonitor());
            eventBusTemplateBeanDefinitionBuilder.addPropertyValue("eventBusInstance", v.getEventBusInstance());
            eventBusTemplateBeanDefinitionBuilder.addPropertyReference("eventListenerFactory",
                    "eventListenerFactory");
            defaultListableBeanFactory.registerBeanDefinition(eventBusTemplateName,
                    eventBusTemplateBeanDefinitionBuilder.getRawBeanDefinition());
        });
    }

}
