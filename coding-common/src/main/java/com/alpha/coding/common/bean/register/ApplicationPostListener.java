package com.alpha.coding.common.bean.register;

/**
 * ApplicationPostListener
 *
 * @version 1.0
 * @date 2021年05月10日
 */
public interface ApplicationPostListener {

    /**
     * 注册回调函数
     */
    void registerPostCallback(Runnable runnable);

}
