package com.alpha.coding.bo.function;

import java.util.function.BooleanSupplier;

/**
 * ValidateDelegate
 *
 * @version 1.0
 * Date: 2021/5/11
 */
public interface ValidateDelegate {

    /**
     * 代理校验请求
     */
    boolean delegate(BooleanSupplier supplier);

}
