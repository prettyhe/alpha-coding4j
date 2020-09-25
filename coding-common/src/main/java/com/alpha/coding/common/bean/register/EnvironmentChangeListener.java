package com.alpha.coding.common.bean.register;

import java.util.EventListener;

/**
 * EnvironmentChangeListener
 *
 * @version 1.0
 * Date: 2020/9/15
 */
public interface EnvironmentChangeListener extends EventListener {

    /**
     * 环境配置变化监听
     */
    void onChange(EnvironmentChangeEvent event);

}
