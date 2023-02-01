package com.alpha.coding.bo.function;

/**
 * DefaultProvider
 *
 * @version 1.0
 * Date: 2023/1/11
 */
public interface DefaultProvider<DEFAULT extends DefaultProvider<DEFAULT>> {

    /**
     * 提供默认实例，默认不提供
     */
    default DEFAULT provideDefault() {
        return null;
    }

}
