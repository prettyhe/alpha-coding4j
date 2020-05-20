package com.alpha.coding.common.bean.spi;

public interface ConfigurationRegisterHandler extends Ordered {

    /**
     * 处理注册
     */
    void registerBeanDefinitions(RegisterBeanDefinitionContext context);

}
