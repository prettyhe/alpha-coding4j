package com.alpha.coding.common.bean.init;

import java.util.List;

/**
 * AsyncWarmUpCallback
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public interface AsyncWarmUpCallback {

    /**
     * 异步暖启动回调
     */
    List<Runnable> asyncWarmUp();

}
