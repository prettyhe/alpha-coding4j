package com.alpha.coding.common.log;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.core.annotation.AnnotationAttributes;

import com.alpha.coding.common.aop.AopBeanDefinitionRegistry;
import com.alpha.coding.common.aop.AspectJParams;
import com.alpha.coding.common.bean.spi.ConfigurationRegisterHandler;
import com.alpha.coding.common.bean.spi.RegisterBeanDefinitionContext;
import com.alpha.coding.common.utils.SpringAnnotationConfigUtils;
import com.alpha.coding.common.utils.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * EnableLogMonitorHandler
 *
 * @version 1.0
 * Date: 2020-03-20
 */
@Slf4j
public class EnableLogMonitorHandler implements ConfigurationRegisterHandler {

    private static final AtomicInteger COUNT = new AtomicInteger(0);

    @Override
    public void registerBeanDefinitions(RegisterBeanDefinitionContext context) {
        Set<AnnotationAttributes> annotationAttributes = SpringAnnotationConfigUtils.attributesForRepeatable(
                context.getImportingClassMetadata(), EnableLogMonitors.class, EnableLogMonitor.class);
        if (CollectionUtils.isEmpty(annotationAttributes)) {
            return;
        }
        for (AnnotationAttributes attributes : annotationAttributes) {
            // 注册AOP ref bean
            String beanName = LogMonitorAop.class.getName() + "_AUTO_" + COUNT.incrementAndGet();
            BeanDefinitionBuilder beanDefinitionBuilder =
                    BeanDefinitionBuilder.genericBeanDefinition(LogMonitorAop.class);
            beanDefinitionBuilder.addDependsOn(attributes.getString("logorBean"));
            beanDefinitionBuilder.addPropertyReference("logor", attributes.getString("logorBean"));
            // 从注解中获取LogMonitorAop基本配置
            final AnnotationAttributes logConfig = attributes.getAnnotation("logConfig");
            beanDefinitionBuilder.addPropertyValue("logType", logConfig.getEnum("logType"));
            beanDefinitionBuilder.addPropertyValue("customLogType",
                    logConfig.getString("customLogType"));
            beanDefinitionBuilder.addPropertyValue("isRequestLog",
                    logConfig.getBoolean("isRequestLog"));
            beanDefinitionBuilder.addPropertyValue("isResponseLog",
                    logConfig.getBoolean("isResponseLog"));
            beanDefinitionBuilder.addPropertyValue("useItsLog",
                    logConfig.getBoolean("useItsLog"));
            beanDefinitionBuilder.addPropertyValue("excludeInfoKeys",
                    Arrays.stream(logConfig.getStringArray("excludeInfoKeys"))
                            .reduce((x, y) -> x + "," + y).get());
            beanDefinitionBuilder.addPropertyValue("extraMsgSupplier",
                    ExtraMsgSupplierCache.getDefault(logConfig.getClass("extraMsgSupplier")));
            context.getRegistry().registerBeanDefinition(beanName, beanDefinitionBuilder.getRawBeanDefinition());
            // 注册AOP
            final AspectJParams params = new AspectJParams()
                    .setProxyTargetClass(attributes.getBoolean("proxyTargetClass"))
                    .setAopRefBean(beanName)
                    .setAopOrder(attributes.getNumber("order").intValue())
                    .setAopAdvice("around")
                    .setAopAdviceMethod("doMonitor")
                    .setAopPointcut(Arrays.stream(attributes.getStringArray("pointcut"))
                            .filter(StringUtils::isNotEmpty)
                            .reduce((x, y) -> x + " " + y).get());
            try {
                AopBeanDefinitionRegistry.loadBeanDefinitions(context.getRegistry(), params);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }
}