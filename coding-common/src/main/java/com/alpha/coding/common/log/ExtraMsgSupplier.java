package com.alpha.coding.common.log;

import java.util.function.Supplier;

/**
 * ExtraMsgSupplier
 *
 * @version 1.0
 * Date: 2020/5/14
 */
public interface ExtraMsgSupplier {

    /**
     * 附加消息提供函数
     */
    default Supplier<String> supplier() {
        return () -> null;
    }

}
