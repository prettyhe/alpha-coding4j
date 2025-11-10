package com.alpha.coding.common.dubbo;

import com.alpha.coding.bo.function.OneTimeSupplierHolder;
import com.alpha.coding.common.utils.ClassUtils;

/**
 * DubboContextTool
 *
 * @version 1.0
 * @date 2025年04月27日
 */
public class DubboContextTool {

    private static final OneTimeSupplierHolder<Class<?>> APACHE_SPRING_EXTENSION_FACTORY_CLASS =
            OneTimeSupplierHolder.of(() -> ClassUtils
                    .findClass("org.apache.dubbo.config.spring.extension.SpringExtensionFactory",
                            true, DubboContextTool.class));

    public static boolean useApacheDubbo() {
        return APACHE_SPRING_EXTENSION_FACTORY_CLASS.get() != null;
    }

}
