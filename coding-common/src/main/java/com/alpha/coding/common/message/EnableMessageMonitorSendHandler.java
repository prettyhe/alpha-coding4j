package com.alpha.coding.common.message;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.core.annotation.AnnotationAttributes;

import com.alpha.coding.common.bean.register.BeanDefinitionRegistryUtils;
import com.alpha.coding.common.bean.register.EnvironmentChangeEvent;
import com.alpha.coding.common.bean.register.EnvironmentChangeListener;
import com.alpha.coding.common.bean.spi.ConfigurationRegisterHandler;
import com.alpha.coding.common.bean.spi.RegisterBeanDefinitionContext;
import com.alpha.coding.common.message.task.MessagePublishCompensateTask;

/**
 * EnableMessageMonitorSendHandler
 *
 * @version 1.0
 * Date: 2021/9/8
 */
public class EnableMessageMonitorSendHandler implements ConfigurationRegisterHandler, EnvironmentChangeListener {

    @Override
    public void onChange(EnvironmentChangeEvent event) {
        final RegisterBeanDefinitionContext context = (RegisterBeanDefinitionContext) event.getSource();
        registerBeanDefinitions(context);
    }

    @Override
    public void registerBeanDefinitions(RegisterBeanDefinitionContext context) {
        AnnotationAttributes attributes = AnnotationAttributes
                .fromMap(context.getImportingClassMetadata()
                        .getAnnotationAttributes(EnableMessageMonitorSend.class.getName()));
        if (attributes == null || attributes.isEmpty()) {
            return;
        }
        // 注册DependencyHolder
        BeanDefinitionBuilder dependencyDefinitionBuilder =
                BeanDefinitionBuilder.genericBeanDefinition(DependencyHolder.class);
        dependencyDefinitionBuilder.addPropertyValue("bizGroup",
                attributes.getString("bizGroup"));
        dependencyDefinitionBuilder.addPropertyReference("dataSource",
                attributes.getString("dataSource"));
        dependencyDefinitionBuilder.addPropertyReference("redisTemplate",
                attributes.getString("redisTemplate"));
        dependencyDefinitionBuilder.addPropertyReference("messagePublishAdaptor",
                attributes.getString("messagePublishAdaptor"));
        BeanDefinitionRegistryUtils.overideBeanDefinition(context.getRegistry(),
                "messageDependencyHolder", dependencyDefinitionBuilder.getBeanDefinition());
        if (attributes.getBoolean("enableCompensateTask")) {
            BeanDefinitionBuilder compensateTaskBeanDefinitionBuilder =
                    BeanDefinitionBuilder.genericBeanDefinition(MessagePublishCompensateTask.class);
            compensateTaskBeanDefinitionBuilder.addPropertyValue("synchronizedExecTask",
                    attributes.getBoolean("synchronizedCompensateTask"));
            BeanDefinitionRegistryUtils.overideBeanDefinition(context.getRegistry(),
                    "messagePublishCompensateTask", compensateTaskBeanDefinitionBuilder.getBeanDefinition());
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
