package com.alpha.coding.common.message.publish;

import com.alpha.coding.common.message.dal.MessageMonitor;

/**
 * MessagePublishListener
 *
 * @version 1.0
 * Date: 2021/7/3
 */
public interface MessagePublishListener {

    /**
     * 成功监听
     */
    void onSuccess(MessageMonitor monitor);

    /**
     * 失败监听
     */
    void onFail(MessageMonitor monitor, Throwable throwable);

}
