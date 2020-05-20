package com.alpha.coding.common.aop;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;

import com.alpha.coding.common.utils.IOUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * AopBeanDefinitionRegistry
 *
 * @version 1.0
 * Date: 2020-03-20
 */
@Slf4j
public class AopBeanDefinitionRegistry {

    public static int loadBeanDefinitions(BeanDefinitionRegistry registry, AspectJParams params)
            throws BeanDefinitionStoreException, IOException {
        final ClassPathResource classPathResource = new ClassPathResource("/resource/aop-definition-template.tpl");
        String template = null;
        try (InputStream inputStream = classPathResource.getInputStream()) {
            template = IOUtils.readFromInputStream(inputStream, Charset.forName("UTF-8"), true);
        }
        if (template == null || template.isEmpty()) {
            throw new RuntimeException("AOP template empty");
        }
        String defineText = new StringSubstitutor(params.build()).replace(template);
        if (log.isTraceEnabled()) {
            log.debug("AOP define: \n{}", defineText);
        }
        ByteArrayResource byteArrayResource = new ByteArrayResource(defineText.getBytes());
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(registry);
        return reader.loadBeanDefinitions(byteArrayResource);
    }

}
