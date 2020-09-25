package com.alpha.coding.common.activemq;

import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;

import com.alpha.coding.common.bean.register.BeanDefinitionRegistryUtils;
import com.alpha.coding.common.bean.register.EnvironmentChangeEvent;
import com.alpha.coding.common.bean.register.EnvironmentChangeListener;
import com.alpha.coding.common.bean.spi.ConfigurationRegisterHandler;
import com.alpha.coding.common.bean.spi.RegisterBeanDefinitionContext;
import com.alpha.coding.common.utils.SpringAnnotationConfigUtils;
import com.alpha.coding.common.utils.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * EnableActiveMQAutoConfigHandler
 *
 * @version 1.0
 * Date: 2020/4/4
 */
@Slf4j
public class EnableActiveMQAutoConfigHandler implements ConfigurationRegisterHandler, EnvironmentChangeListener {

    @Override
    public void registerBeanDefinitions(RegisterBeanDefinitionContext context) {
        Set<AnnotationAttributes> annotationAttributes = SpringAnnotationConfigUtils.attributesForRepeatable(
                context.getImportingClassMetadata(), EnableActiveMQAutoConfigs.class, EnableActiveMQAutoConfig.class);
        if (CollectionUtils.isEmpty(annotationAttributes)) {
            return;
        }
        final Environment env = context.getEnvironment();
        final BeanDefinitionRegistry registry = context.getRegistry();
        for (AnnotationAttributes attributes : annotationAttributes) {
            final String[] envPrefixes = attributes.getStringArray("prefix");
            if (envPrefixes.length == 0) {
                continue;
            }
            for (String prefix : envPrefixes) {
                // registerBeanDefinition: ActiveMQConnectionFactory
                BeanDefinitionBuilder activeMQConnectionFactory = BeanDefinitionBuilder
                        .genericBeanDefinition("org.apache.activemq.ActiveMQConnectionFactory");
                final String user = env.getProperty(prefix + ".user");
                final String password = env.getProperty(prefix + ".password");
                final String brokerUrl = env.getProperty(prefix + ".broker-url");
                activeMQConnectionFactory.setScope(BeanDefinition.SCOPE_SINGLETON);
                activeMQConnectionFactory.addConstructorArgValue(user);
                activeMQConnectionFactory.addConstructorArgValue(password);
                activeMQConnectionFactory.addConstructorArgValue(brokerUrl);
                BeanDefinitionRegistryUtils.overideBeanDefinition(registry,
                        prefix + "ActiveMQConnectionFactory",
                        activeMQConnectionFactory.getBeanDefinition());
                // registerBeanDefinition: PooledConnectionFactoryBean
                BeanDefinitionBuilder pooledConnectionFactoryBean = BeanDefinitionBuilder
                        .genericBeanDefinition("org.apache.activemq.pool.PooledConnectionFactoryBean");
                pooledConnectionFactoryBean.setScope(BeanDefinition.SCOPE_SINGLETON);
                pooledConnectionFactoryBean.addPropertyReference("connectionFactory",
                        prefix + "ActiveMQConnectionFactory");
                pooledConnectionFactoryBean.addPropertyValue("maxConnections",
                        env.getProperty(prefix + ".pool.max-connections", Integer.class, 1));
                BeanDefinitionRegistryUtils.overideBeanDefinition(registry,
                        prefix + "PooledConnectionFactoryBean",
                        pooledConnectionFactoryBean.getBeanDefinition());
                // registerBeanDefinition: topic
                final String enableTopics = env.getProperty(prefix + ".topic.enables");
                if (StringUtils.isNotBlank(enableTopics)) {
                    for (String topic : enableTopics.split(",")) {
                        final String producer = env.getProperty(prefix + ".topic." + topic + ".producer");
                        if (StringUtils.isNotBlank(producer)) {
                            // registerBeanDefinition: ActiveMQTopic
                            BeanDefinitionBuilder producerActiveMQTopic = BeanDefinitionBuilder
                                    .genericBeanDefinition("org.apache.activemq.command.ActiveMQTopic");
                            producerActiveMQTopic.addConstructorArgValue(producer);
                            BeanDefinitionRegistryUtils.overideBeanDefinition(registry,
                                    prefix + "_" + topic + "ProducerActiveMQTopic",
                                    producerActiveMQTopic.getBeanDefinition());
                            // registerBeanDefinition: JmsTemplate
                            BeanDefinitionBuilder jmsTemplate = BeanDefinitionBuilder
                                    .genericBeanDefinition("org.springframework.jms.core.JmsTemplate");
                            jmsTemplate.addPropertyReference("connectionFactory",
                                    prefix + "PooledConnectionFactoryBean");
                            jmsTemplate.addPropertyValue("pubSubDomain", true);
                            jmsTemplate.addPropertyReference("defaultDestination",
                                    prefix + "_" + topic + "ProducerActiveMQTopic");
                            String jmsTemplateBeanName =
                                    env.getProperty(prefix + ".topic." + topic + ".JmsTemplateName",
                                            prefix + "_" + topic + "JmsTemplate");
                            BeanDefinitionRegistryUtils.overideBeanDefinition(registry,
                                    jmsTemplateBeanName, jmsTemplate.getBeanDefinition());
                            log.info("registerBeanDefinition: prefix: {}, topic: {}, class: {}, beanName: {}",
                                    prefix, topic, "org.springframework.jms.core.JmsTemplate", jmsTemplateBeanName);
                        }
                        final String consumer = env.getProperty(prefix + ".topic." + topic + ".consumer");
                        if (StringUtils.isNotBlank(consumer)) {
                            // registerBeanDefinition: ActiveMQTopic
                            BeanDefinitionBuilder consumerActiveMQTopic = BeanDefinitionBuilder
                                    .genericBeanDefinition("org.apache.activemq.command.ActiveMQTopic");
                            consumerActiveMQTopic.addConstructorArgValue(consumer);
                            BeanDefinitionRegistryUtils.overideBeanDefinition(registry,
                                    prefix + "_" + topic + "ConsumerActiveMQTopic",
                                    consumerActiveMQTopic.getBeanDefinition());
                            // registerBeanDefinition: DefaultMessageListenerContainer
                            BeanDefinitionBuilder defaultMessageListenerContainer = BeanDefinitionBuilder
                                    .genericBeanDefinition(
                                            "org.springframework.jms.listener.DefaultMessageListenerContainer");
                            defaultMessageListenerContainer.addPropertyReference("connectionFactory",
                                    prefix + "PooledConnectionFactoryBean");
                            defaultMessageListenerContainer.addPropertyReference("destination",
                                    prefix + "_" + topic + "ConsumerActiveMQTopic");
                            defaultMessageListenerContainer.addPropertyReference("messageListener",
                                    env.getProperty(prefix + ".topic." + topic + ".listenerBean"));
                            final String consumerConcurrency =
                                    env.getProperty(prefix + ".topic." + topic + ".consumerConcurrency");
                            if (StringUtils.isNotBlank(consumerConcurrency)) {
                                defaultMessageListenerContainer.addPropertyValue("concurrency", consumerConcurrency);
                            }
                            BeanDefinitionRegistryUtils.overideBeanDefinition(registry,
                                    prefix + "_" + topic + "DefaultMessageListenerContainer",
                                    defaultMessageListenerContainer.getBeanDefinition());
                        }
                    }
                }
                // registerBeanDefinition: queue
                final String enableQueues = env.getProperty(prefix + ".queue.enables");
                if (StringUtils.isNotBlank(enableQueues)) {
                    for (String queue : enableQueues.split(",")) {
                        final String producer = env.getProperty(prefix + ".queue." + queue + ".producer");
                        if (StringUtils.isNotBlank(producer)) {
                            // registerBeanDefinition: ActiveMQQueue
                            BeanDefinitionBuilder producerActiveMQQueue = BeanDefinitionBuilder
                                    .genericBeanDefinition("org.apache.activemq.command.ActiveMQQueue");
                            producerActiveMQQueue.addConstructorArgValue(producer);
                            BeanDefinitionRegistryUtils.overideBeanDefinition(registry,
                                    prefix + "_" + queue + "ProducerActiveMQQueue",
                                    producerActiveMQQueue.getBeanDefinition());
                            // registerBeanDefinition: JmsTemplate
                            BeanDefinitionBuilder jmsTemplate = BeanDefinitionBuilder
                                    .genericBeanDefinition("org.springframework.jms.core.JmsTemplate");
                            jmsTemplate.addPropertyReference("connectionFactory",
                                    prefix + "PooledConnectionFactoryBean");
                            jmsTemplate.addPropertyReference("defaultDestination",
                                    prefix + "_" + queue + "ProducerActiveMQQueue");
                            String jmsTemplateBeanName =
                                    env.getProperty(prefix + ".queue." + queue + ".JmsTemplateName",
                                            prefix + "_" + queue + "JmsTemplate");
                            BeanDefinitionRegistryUtils.overideBeanDefinition(registry,
                                    jmsTemplateBeanName, jmsTemplate.getBeanDefinition());
                            log.info("registerBeanDefinition: prefix: {}, queue: {}, class: {}, beanName: {}",
                                    prefix, queue, "org.springframework.jms.core.JmsTemplate", jmsTemplateBeanName);
                        }
                        final String consumer = env.getProperty(prefix + ".queue." + queue + ".consumer");
                        if (StringUtils.isNotBlank(consumer)) {
                            // registerBeanDefinition: ActiveMQQueue
                            BeanDefinitionBuilder consumerActiveMQQueue = BeanDefinitionBuilder
                                    .genericBeanDefinition("org.apache.activemq.command.ActiveMQQueue");
                            consumerActiveMQQueue.addConstructorArgValue(consumer);
                            BeanDefinitionRegistryUtils.overideBeanDefinition(registry,
                                    prefix + "_" + queue + "ConsumerActiveMQQueue",
                                    consumerActiveMQQueue.getBeanDefinition());
                            // registerBeanDefinition: DefaultMessageListenerContainer
                            BeanDefinitionBuilder defaultMessageListenerContainer = BeanDefinitionBuilder
                                    .genericBeanDefinition(
                                            "org.springframework.jms.listener.DefaultMessageListenerContainer");
                            defaultMessageListenerContainer.addPropertyReference("connectionFactory",
                                    prefix + "PooledConnectionFactoryBean");
                            defaultMessageListenerContainer.addPropertyReference("destination",
                                    prefix + "_" + queue + "ConsumerActiveMQQueue");
                            defaultMessageListenerContainer.addPropertyReference("messageListener",
                                    env.getProperty(prefix + ".queue." + queue + ".listenerBean"));
                            final String consumerConcurrency =
                                    env.getProperty(prefix + ".queue." + queue + ".consumerConcurrency");
                            if (StringUtils.isNotBlank(consumerConcurrency)) {
                                defaultMessageListenerContainer.addPropertyValue("concurrency", consumerConcurrency);
                            }
                            BeanDefinitionRegistryUtils.overideBeanDefinition(registry,
                                    prefix + "_" + queue + "DefaultMessageListenerContainer",
                                    defaultMessageListenerContainer.getBeanDefinition());
                        }
                    }
                }
            }
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public void onChange(EnvironmentChangeEvent event) {
        final RegisterBeanDefinitionContext context = (RegisterBeanDefinitionContext) event.getSource();
        registerBeanDefinitions(context);
    }

}
