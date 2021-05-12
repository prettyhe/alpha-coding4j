package com.alpha.coding.common.rocketmq;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;

import com.alpha.coding.common.bean.register.ApplicationPostListener;
import com.alpha.coding.common.bean.register.BeanDefineUtils;
import com.alpha.coding.common.bean.register.BeanDefinitionRegistryUtils;
import com.alpha.coding.common.bean.register.EnvironmentChangeEvent;
import com.alpha.coding.common.bean.register.EnvironmentChangeListener;
import com.alpha.coding.common.bean.spi.ConfigurationRegisterHandler;
import com.alpha.coding.common.bean.spi.RegisterBeanDefinitionContext;
import com.alpha.coding.common.utils.SpringAnnotationConfigUtils;
import com.alpha.coding.common.utils.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * EnableRocketMQAutoConfigHandler
 *
 * @version 1.0
 * Date: 2020/4/28
 */
@Slf4j
public class EnableRocketMQAutoConfigHandler implements ConfigurationRegisterHandler, EnvironmentChangeListener {

    @Override
    public void registerBeanDefinitions(RegisterBeanDefinitionContext context) {
        registerProducer(context);
        registerConsumer(context);
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

    /**
     * 注册生产者
     */
    private void registerProducer(RegisterBeanDefinitionContext context) {
        Set<AnnotationAttributes> annotationAttributes = SpringAnnotationConfigUtils.attributesForRepeatable(
                context.getImportingClassMetadata(), EnableRocketMQProducers.class, EnableRocketMQProducer.class);
        if (CollectionUtils.isEmpty(annotationAttributes)) {
            return;
        }
        final Environment env = context.getEnvironment();
        final BeanDefinitionRegistry registry = context.getRegistry();
        for (AnnotationAttributes attributes : annotationAttributes) {
            final String producerBeanName = attributes.getString("producerBeanName");
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
                    .genericBeanDefinition(DefaultMQProducer.class);
            String namesrvAddr = Arrays.stream(attributes.getStringArray("namesrvAddr"))
                    .map(x -> BeanDefineUtils.resolveValue(context, x, String.class))
                    .filter(StringUtils::isNotBlank).findFirst().orElse(null);
            if (namesrvAddr == null) {
                namesrvAddr = env.getProperty("rocketmq.nameserver");
            }
            final String group = BeanDefineUtils.resolveValue(context, attributes.getString("group"), String.class);
            beanDefinitionBuilder.addPropertyValue("producerGroup", group);
            beanDefinitionBuilder.addPropertyValue("namesrvAddr", namesrvAddr);
            beanDefinitionBuilder.addPropertyValue("instanceName", UUID.randomUUID().toString().replaceAll("-", ""));
            if (env.containsProperty("rocketmq.producer.retryTimesWhenSendFailed")) {
                beanDefinitionBuilder.addPropertyValue("retryTimesWhenSendFailed",
                        env.getProperty("rocketmq.producer.retryTimesWhenSendFailed", Integer.class));
            }
            beanDefinitionBuilder.setInitMethodName("start");
            beanDefinitionBuilder.setDestroyMethodName("shutdown");
            BeanDefinitionRegistryUtils.overideBeanDefinition(registry,
                    producerBeanName, beanDefinitionBuilder.getBeanDefinition());
            if (registry.containsBeanDefinition("InternalApplicationPostListener")) {
                context.getBeanFactory().getBean("InternalApplicationPostListener", ApplicationPostListener.class)
                        .registerPostCallback(() -> context.getBeanFactory().getBean(producerBeanName));
            }
        }
    }

    /**
     * 注册消费者
     */
    private void registerConsumer(RegisterBeanDefinitionContext context) {
        Set<AnnotationAttributes> annotationAttributes = SpringAnnotationConfigUtils.attributesForRepeatable(
                context.getImportingClassMetadata(), EnableRocketMQConsumers.class, EnableRocketMQConsumer.class);
        if (CollectionUtils.isEmpty(annotationAttributes)) {
            return;
        }
        final Environment env = context.getEnvironment();
        final BeanDefinitionRegistry registry = context.getRegistry();
        for (AnnotationAttributes attributes : annotationAttributes) {
            final String group = BeanDefineUtils.resolveValue(context, attributes.getString("group"), String.class);
            final String topic = BeanDefineUtils.resolveValue(context, attributes.getString("topic"), String.class);
            final String tag = BeanDefineUtils.resolveValue(context, attributes.getString("tag"), String.class);
            String namesrvAddr = Arrays.stream(attributes.getStringArray("namesrvAddr"))
                    .map(x -> BeanDefineUtils.resolveValue(context, x, String.class))
                    .filter(StringUtils::isNotBlank).findFirst().orElse(null);
            if (namesrvAddr == null) {
                namesrvAddr = (String) BeanDefineUtils.fetchProperty(env,
                        Arrays.asList("rocketmq.nameserver." + topic, "rocketmq.nameserver"), StringUtils::isNotBlank,
                        String.class, null);
            }
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
                    .genericBeanDefinition(GenericRocketMQConsumer.class);
            beanDefinitionBuilder.addPropertyValue("consumerGroup", group);
            beanDefinitionBuilder.addPropertyValue("topic", topic);
            beanDefinitionBuilder.addPropertyValue("tag", tag);
            beanDefinitionBuilder.addPropertyValue("namesrvAddr", namesrvAddr);
            beanDefinitionBuilder.addPropertyReference("messageListener",
                    attributes.getString("messageListenerBeanName"));
            Optional.ofNullable(BeanDefineUtils.fetchProperty(env,
                    Arrays.asList("rocketmq.consumer." + topic + ".pullInterval",
                            "rocketmq.consumer.pullInterval"), StringUtils::isNumeric,
                    Long.class, null))
                    .ifPresent(x -> beanDefinitionBuilder.addPropertyValue("pullInterval", x));
            Optional.ofNullable(BeanDefineUtils.fetchProperty(env,
                    Arrays.asList("rocketmq.consumer." + topic + ".pullBatchSize",
                            "rocketmq.consumer.pullBatchSize"), StringUtils::isNumeric,
                    Integer.class, null))
                    .ifPresent(x -> beanDefinitionBuilder.addPropertyValue("pullBatchSize", x));
            Optional.ofNullable(BeanDefineUtils.fetchProperty(env,
                    Arrays.asList("rocketmq.consumer." + topic + ".consumeMessageBatchMaxSize",
                            "rocketmq.consumer.consumeMessageBatchMaxSize"), StringUtils::isNumeric,
                    Integer.class, null))
                    .ifPresent(x -> beanDefinitionBuilder.addPropertyValue("consumeMessageBatchMaxSize", x));
            Optional.ofNullable(BeanDefineUtils.fetchProperty(env,
                    Arrays.asList("rocketmq.consumer." + topic + ".consumeThreadMin",
                            "rocketmq.consumer.consumeThreadMin"), StringUtils::isNumeric,
                    Integer.class, null))
                    .ifPresent(x -> beanDefinitionBuilder.addPropertyValue("consumeThreadMin", x));
            Optional.ofNullable(BeanDefineUtils.fetchProperty(env,
                    Arrays.asList("rocketmq.consumer." + topic + ".consumeThreadMax",
                            "rocketmq.consumer.consumeThreadMax"), StringUtils::isNumeric,
                    Integer.class, null))
                    .ifPresent(x -> beanDefinitionBuilder.addPropertyValue("consumeThreadMax", x));
            final String consumerBeanName = Optional.ofNullable(attributes.getString("consumerBeanName"))
                    .filter(StringUtils::isNotBlank)
                    .orElse(group + "#" + topic + "#" + tag + "#" + "rocketMQConsumer");
            BeanDefinitionRegistryUtils.overideBeanDefinition(registry,
                    consumerBeanName, beanDefinitionBuilder.getBeanDefinition());
            if (registry.containsBeanDefinition("InternalApplicationPostListener")) {
                context.getBeanFactory().getBean("InternalApplicationPostListener", ApplicationPostListener.class)
                        .registerPostCallback(() -> context.getBeanFactory().getBean(consumerBeanName));
            }
        }
    }
}
