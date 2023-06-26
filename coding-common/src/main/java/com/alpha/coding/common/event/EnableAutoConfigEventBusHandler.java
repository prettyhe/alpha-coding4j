package com.alpha.coding.common.event;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.annotation.AnnotationAttributes;

import com.alpha.coding.common.bean.register.BeanDefineUtils;
import com.alpha.coding.common.bean.spi.ConfigurationRegisterHandler;
import com.alpha.coding.common.bean.spi.RegisterBeanDefinitionContext;
import com.alpha.coding.common.event.configuration.EventConfiguration;
import com.alpha.coding.common.utils.SpringAnnotationConfigUtils;

/**
 * EnableAutoConfigEventBusHandler
 *
 * @version 1.0
 * Date: 2020-03-19
 */
public class EnableAutoConfigEventBusHandler implements ConfigurationRegisterHandler {

    private static final AtomicInteger COUNT = new AtomicInteger(0);

    @Override
    public void registerBeanDefinitions(RegisterBeanDefinitionContext context) {
        Set<AnnotationAttributes> annotationAttributes = SpringAnnotationConfigUtils.attributesForRepeatable(
                context.getImportingClassMetadata(), EnableAutoConfigEventBuss.class, EnableAutoConfigEventBus.class);
        if (annotationAttributes.isEmpty()) {
            return;
        }
        final BeanDefinitionRegistry registry = context.getRegistry();
        for (AnnotationAttributes attribute : annotationAttributes) {
            final Class<?>[] identities = attribute.getClassArray("eventIdentity");
            for (Class<?> identity : identities) {
                String beanName = EventConfiguration.class.getName() + "_AUTO_" + COUNT.incrementAndGet();
                BeanDefinitionBuilder beanDefinitionBuilder =
                        BeanDefinitionBuilder.genericBeanDefinition(EventConfiguration.class);
                beanDefinitionBuilder.addPropertyValue("identity", identity);
                if (attribute.getBoolean("useDefaultBusInstance")) {
                    beanDefinitionBuilder.addPropertyReference("eventBusInstance", "defaultEventBusInstance");
                } else {
                    beanDefinitionBuilder.addPropertyReference("eventBusInstance",
                            attribute.getString("eventBusInstanceName"));
                }
                beanDefinitionBuilder.addPropertyValue("pollingEventInterval", BeanDefineUtils
                        .resolveValue(context, attribute.getString("pollingEventInterval"), Integer.class));
                beanDefinitionBuilder.addPropertyValue("enableAsyncPost", BeanDefineUtils
                        .resolveValue(context, attribute.getString("enableAsyncPost"), Boolean.class));
                beanDefinitionBuilder.addPropertyValue("enableEventPostMonitor", BeanDefineUtils
                        .resolveValue(context, attribute.getString("enableEventPostMonitor"), Boolean.class));
                registry.registerBeanDefinition(beanName, beanDefinitionBuilder.getRawBeanDefinition());
            }
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }

}
