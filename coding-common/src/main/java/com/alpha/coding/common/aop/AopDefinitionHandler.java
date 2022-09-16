package com.alpha.coding.common.aop;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.annotation.AnnotationAttributes;

import com.alpha.coding.common.bean.spi.ConfigurationRegisterHandler;
import com.alpha.coding.common.bean.spi.RegisterBeanDefinitionContext;
import com.alpha.coding.common.utils.SpringAnnotationConfigUtils;
import com.alpha.coding.common.utils.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * AopDefinitionHandler
 *
 * @version 1.0
 * Date: 2020-03-20
 */
@Slf4j
public class AopDefinitionHandler implements ConfigurationRegisterHandler {

    @Override
    public void registerBeanDefinitions(RegisterBeanDefinitionContext context) {
        Set<AnnotationAttributes> annotationAttributes = SpringAnnotationConfigUtils.attributesForRepeatable(
                context.getImportingClassMetadata(), AopDefinitions.class, AopDefinition.class);
        if (CollectionUtils.isEmpty(annotationAttributes)) {
            return;
        }
        for (AnnotationAttributes attributes : annotationAttributes) {
            final AspectJParams params = new AspectJParams()
                    .setProxyTargetClass(attributes.getBoolean("proxyTargetClass"))
                    .setAopRefBean(attributes.getString("refBeanName"))
                    .setAopOrder(attributes.getNumber("order").intValue())
                    .setAopAdvice(attributes.getString("advice"))
                    .setAopAdviceMethod(attributes.getString("adviceMethod"))
                    .setAopPointcut(Arrays.stream(attributes.getStringArray("pointcut"))
                            .filter(StringUtils::isNotEmpty)
                            .reduce("", (x, y) -> x + " " + y));
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
